package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Locale

class MediaAdapter(
    private val items: List<MediaItem>,
    private val isGrid: Boolean,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val icon: ImageView = view.findViewById(android.R.id.icon)
        val duration: TextView? = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Using Android's default simple layouts to prevent any XML missing errors
        val layoutId = if (isGrid) android.R.layout.activity_list_item else android.R.layout.simple_list_item_2
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        
        // Icon logic
        if (item.isVideo) {
            holder.icon.setImageResource(android.R.drawable.ic_media_play)
        } else {
            holder.icon.setImageResource(android.R.drawable.ic_media_audio)
        }

        // Format Duration
        val totalSecs = item.duration / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        holder.duration?.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
