package com.bignerdranch.android.treespotter_firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    val CURRENT_FRAGMENT_BUNDLE_KEY = "current fragment bundle key"
    var currentFragmentTag = "MAP"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentFragmentTag = savedInstanceState?.getString(CURRENT_FRAGMENT_BUNDLE_KEY) ?: "MAP"

        showFragment(currentFragmentTag) // first shows the Map Fragment

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.show_map -> {
                    showFragment("MAP")
                    true
                }
                R.id.show_list -> {
                    showFragment("LIST")
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun showFragment(tag: String) {
        // if we are not seeing the fragment with the given tag, display it

        currentFragmentTag = tag

        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            val transaction = supportFragmentManager.beginTransaction()
            when(tag) {
                "MAP" -> transaction.replace(R.id.fragmentContainerView, TreeMapFragment.newInstance(), "MAP")
                "LIST" -> transaction.replace(R.id.fragmentContainerView, TreeListFragment.newInstance(), "LIST")
            }
            transaction.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_BUNDLE_KEY, currentFragmentTag)
    }
}

// Example - not important
// // to connect to firebase database
// val db = Firebase.firestore
//
// //        val tree = mapOf("name" to "pine", "dateSpotted" to Date())
// //        val tree2 = mapOf("name" to "oak", "dateSpotted" to Date(), "favorite" to true)
// //        db.collection("trees").add(tree)
// //        db.collection("trees").add(tree2)
//
// //        val tree = Tree("Pine", Date())
// //        db.collection("trees").add(tree)
//
// //        db.collection("tees").get().addOnSuccessListener { treeDocuments ->
// //            /* connecting to the database and getting information from each document one at a time from a collection*/
// //            for (treeDoc in treeDocuments) {
// //                val name = treeDoc["name"]
// //                val dateSpotted = treeDoc["dateSpotted"]
// //                val favorite = treeDoc["favorite"]
// //                val path = treeDoc.reference.path
// //                Log.d(TAG, "$name, $dateSpotted, $favorite, $path")
// //            }
// //        }
//
// db.collection("trees")
// .whereEqualTo("name", "Pine")
// .whereEqualTo("favorite", true)
// .orderBy("dateSpotted", Query.Direction.DESCENDING) // most recent first
// .limit(3) // most recent three
// .addSnapshotListener { treeDocuments, error ->
//    /* This listener watches the trees collection on firebase database and executes everytime there is changes in the collection data and gets all the documents*/
//    if (error != null) {
//        Log.e(TAG, "Error getting all trees", error)
//    }
//
//    if (treeDocuments != null) {
//        for (treeDoc in treeDocuments) {
//            val treeFromFirebase = treeDoc.toObject(Tree::class.java)
// //                    val name = treeDoc["name"]
// //                    val dateSpotted = treeDoc["dateSpotted"]
// //                    val favorite = treeDoc["favorite"]
//            val path = treeDoc.reference.path
//            Log.d(TAG, "$treeFromFirebase, $path")
//        }
//    }
// }
