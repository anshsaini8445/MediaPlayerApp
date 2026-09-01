package com.app.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class MediaAdapter(
    private val items: List<MediaItem>,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val duration: TextView = view.findViewById(R.id.tvDuration)
        val thumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        val minutes = (item.duration / 1000) / 60
        val seconds = (item.duration / 1000) % 60
        holder.duration.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        // Glide se video ka thumbnail load karna
        if (item.isVideo) {
            Glide.with(holder.itemView.context)
                .load(item.path)
                .centerCrop()
                .placeholder(android.R.color.darker_gray) // Error fixed here!
                .into(holder.thumbnail)
        } else {
            // Audio ke liye default play icon
            holder.thumbnail.setImageResource(android.R.drawable.ic_media_play)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
