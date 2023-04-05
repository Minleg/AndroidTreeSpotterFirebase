package com.bignerdranch.android.treespotter_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TreeMapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_tree_map, container, false)

        return mainView
    }

    companion object {
        @JvmStatic
        fun newInstance() = TreeMapFragment()
    }
}
