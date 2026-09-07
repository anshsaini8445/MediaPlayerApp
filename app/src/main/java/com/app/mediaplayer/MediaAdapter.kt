package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MediaAdapter(
    private val items: List<MediaItem>,
    private val isGrid: Boolean,
    private val onClick: (MediaItem) -> Unit,
    private val onMoreClick: ((MediaItem) -> Unit)? = null
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView? = view.findViewById(android.R.id.text1)
        val duration: TextView? = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (isGrid) android.R.layout.simple_list_item_1 else android.R.layout.simple_list_item_2
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title?.text = item.title
        
        val totalSecs = item.duration / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        
        holder.duration?.text = if (item.isVideo) "Video • $timeString" else "Audio • $timeString"

        // Single click to play media
        holder.itemView.setOnClickListener { onClick(item) }
        
        // Long press to open the 3-dot options menu
        holder.itemView.setOnLongClickListener {
            onMoreClick?.invoke(item)
            true
        }
    }

    override fun getItemCount() = items.size
}
