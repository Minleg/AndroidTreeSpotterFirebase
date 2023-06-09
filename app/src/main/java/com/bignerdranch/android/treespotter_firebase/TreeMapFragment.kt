package com.bignerdranch.android.treespotter_firebase

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
// import androidx.lifecycle.viewmodel.CreationExtras.Empty.map
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.util.*

private const val TAG = "TREE_MAP_FRAGMENT"

class TreeMapFragment : Fragment() {

    private lateinit var addTreeButton: FloatingActionButton

    private var locationPermissionGranted = false

    private var moveMapToUsersLocation = false

    private var fusedLocationProvider: FusedLocationProviderClient? = null

    private var map: GoogleMap? = null

    private val treeMarkers = mutableListOf<Marker>()

    private var treeList = listOf<Tree>()

    private val treeViewModel: TreeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(TreeViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->

        Log.d(TAG, "Google map ready")
        map = googleMap

        googleMap.setOnInfoWindowClickListener { marker ->
            val treeForMarker = marker.tag as Tree
            requestDeleteTree(treeForMarker)
        }
        updateMap()
    }

    private fun requestDeleteTree(tree: Tree) {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.confirm_delete_tree, tree.name))
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                treeViewModel.deleteTree(tree)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, id ->
                // do nothing
            }
            .create()
            .show()
    }

    private fun updateMap() {
        //  draw markers
        drawTrees()

        if (locationPermissionGranted) {
            if (!moveMapToUsersLocation) {
                moveMapToUsersLocation
            }
        }
        // draw blue dot at user's location
        // show no location message if location permission not granted.
        // or device does not have location enabled
    }

    private fun setAddTreeButtonEnabled(isEnabled: Boolean) {
        addTreeButton.isClickable = isEnabled
        addTreeButton.isEnabled = isEnabled

        if (isEnabled) {
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(
                requireActivity(),
                android.R.color.holo_green_light,
            )
        } else {
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(
                requireActivity(),
                android.R.color.darker_gray,
            )
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun requestLocationPermission() {
        // has user already granted permissions?
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            Log.d(TAG, "permission already granted")
            updateMap()
            setAddTreeButtonEnabled(true)
            fusedLocationProvider =
                LocationServices.getFusedLocationProviderClient(requireActivity()) // initialize the location provider
        } else {
            // need to ask permission
            val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) {
                        Log.d(TAG, "User granted permission")
                        setAddTreeButtonEnabled(true)
                        fusedLocationProvider =
                            LocationServices.getFusedLocationProviderClient(requireActivity())
                    } else {
                        Log.d(TAG, "User didn't grant permission")
                        setAddTreeButtonEnabled(false)
                        locationPermissionGranted = false
                        showSnackbar(getString(R.string.give_permission))
                    }

                    updateMap()
                }

            requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveMapToUserLocation() {
        if (map == null) {
            return
        }

        if (locationPermissionGranted) {
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true

            fusedLocationProvider?.lastLocation?.addOnCompleteListener { getLocationTask ->
                val location = getLocationTask.result
                if (location != null) {
                    Log.d(TAG, "User's location $location")
                    val center = LatLng(location.latitude, location.longitude)
                    val zoomlevel = 8f
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomlevel))
                    moveMapToUsersLocation = true
                } else {
                    showSnackbar(getString(R.string.no_location))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_tree_map, container, false)

        addTreeButton = mainView.findViewById(R.id.add_tree)
        addTreeButton.setOnClickListener {
            // todo add tree at user's location - if location permission granted & location available
            addTreeAtLocation()
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)

        // disable add tree button until location is available
        setAddTreeButtonEnabled(false)

        // request user's permission to access device location
        requestLocationPermission()

        // todo draw existing trees on map
        treeViewModel.latestTrees.observe(requireActivity()) { latestTrees ->
            treeList = latestTrees
            drawTrees()
        }
        return mainView
    }

    @SuppressLint("MissingPermission")
    private fun addTreeAtLocation() {
        if (map == null) {
            return
        }
        if (fusedLocationProvider == null) {
            return
        }
        if (!locationPermissionGranted) {
            showSnackbar(getString(R.string.grant_location_permission))
            return
        }

        fusedLocationProvider?.lastLocation?.addOnCompleteListener(requireActivity()) { locationRequestTask ->

            val location = locationRequestTask.result
            if (location != null) {
                val treeName = getTreeName()
                val tree = Tree(
                    name = treeName,
                    dateSpotted = Date(),
                    location = GeoPoint(location.latitude, location.longitude),
                )
                treeViewModel.addTree(tree)
                moveMapToUserLocation()
                showSnackbar(getString(R.string.added_tree, treeName))
            } else {
                showSnackbar(getString(R.string.no_location))
            }
        }
    }

    private fun drawTrees() {
        if (map == null) {
            return
        }

        for (marker in treeMarkers) {
            marker.remove()
        }

        for (tree in treeList) {
            // make a marker for each tree and add to the map
            tree.location?.let { geoPoint ->

                val isFavorite = tree.favorite ?: false
                val iconId = if (isFavorite) R.drawable.filled_heart_small else R.drawable.tree_small

                val markerOptions = MarkerOptions()
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .title(tree.name)
                    .snippet("Spotted on ${tree.dateSpotted}")
                    .icon(BitmapDescriptorFactory.fromResource(iconId))

                map?.addMarker(markerOptions)?.also { marker ->
                    treeMarkers.add(marker)
                    marker.tag = tree
                }
            }
        }
    }

    private fun getTreeName(): String {
        return listOf("Fir", "Oak", "Pine", "Redwood").random()
        // todo user for name
    }

    companion object {
        @JvmStatic
        fun newInstance() = TreeMapFragment()
    }
}
