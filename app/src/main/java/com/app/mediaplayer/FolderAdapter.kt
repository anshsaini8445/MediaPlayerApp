package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Folder ka data sambhalne ke liye naya class
data class MediaFolder(val name: String, val mediaItems: List<MediaItem>)

class FolderAdapter(
    private val folders: List<MediaFolder>,
    private val onClick: (MediaFolder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderName: TextView = view.findViewById(R.id.tvFolderName)
        val videoCount: TextView = view.findViewById(R.id.tvVideoCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.folderName.text = folder.name
        holder.videoCount.text = "${folder.mediaItems.size} Media Items"
        
        holder.itemView.setOnClickListener { onClick(folder) }
    }

    override fun getItemCount() = folders.size
}
