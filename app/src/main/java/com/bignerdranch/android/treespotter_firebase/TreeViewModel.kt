package com.bignerdranch.android.treespotter_firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "TREE_VIEW_MODEL"

class TreeViewModel : ViewModel() {

    // connect to firebase

    private val db = Firebase.firestore
    private val treeCollectionReference = db.collection("trees")

    val latestTrees = MutableLiveData<List<Tree>>()

    // get all of the tree sightings

    private val latestTreesListener = treeCollectionReference
        .orderBy("dateSpotted", Query.Direction.DESCENDING)
        .limit(10)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching latest trees", error)
            } else if (snapshot != null) {
                val trees = snapshot.toObjects(Tree::class.java) // trees would be list of Tree objects
                Log.d(TAG, "Trees from firebase: $trees")
                latestTrees.postValue(trees) // updates this mutable live data as anything changes on firebase db
            }
        }
}
