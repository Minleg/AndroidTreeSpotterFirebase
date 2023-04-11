package com.bignerdranch.android.treespotter_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass.
 * Use the [TreeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TreeListFragment : Fragment() {

    private val treeViewModel: TreeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(TreeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val recyclerView = inflater.inflate(R.layout.fragment_tree_list, container, false)
        if (recyclerView !is RecyclerView) {
            throw java.lang.RuntimeException("TreeListFragment view should be a recycler view")
        }
        /* sets up the recycler view*/
        val trees = listOf<Tree>()
        val adapter = TreeRecyclerViewAdapter(trees, { tree, isFavorite ->
            treeViewModel.setIsFavorite(tree, isFavorite)
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // anything changes, update with newest trees
        treeViewModel.latestTrees.observe(requireActivity()) { treeList ->
            adapter.trees = treeList
            adapter.notifyDataSetChanged()
        }

        // set up user interface here

        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance() = TreeListFragment()
    }
}
