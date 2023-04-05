package com.bignerdranch.android.treespotter_firebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TreeRecyclerViewAdapter( var trees: List<Tree>) :
    RecyclerView.Adapter<TreeRecyclerViewAdapter.ViewHolder>()
{

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tree: Tree) {
            view.findViewById<TextView>(R.id.tree_name).text = tree.name
            view.findViewById<TextView>(R.id.date_spotted).text = "${tree.dateSpotted}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_tree_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trees.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tree = trees[position]
        holder.bind(tree)
    }
}