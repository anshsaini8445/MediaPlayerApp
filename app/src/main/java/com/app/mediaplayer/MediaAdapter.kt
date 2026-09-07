package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Locale

// Data Classes (Safe declaration)
data class MediaItem(val id: Long, val title: String, val path: String, val duration: Long, val isVideo: Boolean)
data class MediaFolder(val name: String, val mediaItems: List<MediaItem>)

class MediaAdapter(
    private val items: List<MediaItem>,
    private val isGrid: Boolean,
    private val onMoreClick: (MediaItem) -> Unit,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        
        // Time Formatting
        val totalSecs = item.duration / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        holder.tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        // File Size Calculation
        try {
            val file = File(item.path)
            val sizeMb = file.length() / (1024 * 1024)
            holder.tvSubtitle.text = "${sizeMb} MB  •  ${file.parentFile?.name ?: "Unknown"}"
        } catch (e: Exception) {
            holder.tvSubtitle.text = "Unknown Size"
        }

        if (item.isVideo) {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_media_play)
        } else {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_media_ff) 
        }

        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnMore.setOnClickListener { onMoreClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }
}
