package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private val folders: List<MediaFolder>,
    private val onClick: (MediaFolder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.tvName.text = "📁 " + folder.name
        holder.tvName.setTextColor(android.graphics.Color.WHITE)
        
        holder.tvCount.text = "${folder.mediaItems.size} items"
        holder.tvCount.setTextColor(android.graphics.Color.GRAY)

        holder.itemView.setOnClickListener { onClick(folder) }
    }

    override fun getItemCount(): Int = folders.size

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(android.R.id.text1)
        val tvCount: TextView = itemView.findViewById(android.R.id.text2)
    }
}
