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
    private val isGrid: Boolean = false, // Pata lagane ke liye ki Grid hai ya nahi
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LIST = 1
    private val VIEW_TYPE_GRID = 2

    // List Wala Design
    class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val duration: TextView = view.findViewById(R.id.tvDuration)
        val thumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
    }

    // Grid (Naya) Wala Design
    class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitleGrid)
        val duration: TextView = view.findViewById(R.id.tvDurationGrid)
        val thumbnail: ImageView = view.findViewById(R.id.imgThumbnailGrid)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGrid) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_GRID) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_grid, parent, false)
            GridViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
            ListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        
        val minutes = (item.duration / 1000) / 60
        val seconds = (item.duration / 1000) % 60
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        if (holder is ListViewHolder) {
            holder.title.text = item.title
            holder.duration.text = timeStr
            if (item.isVideo) {
                Glide.with(holder.itemView.context).load(item.path).centerCrop().into(holder.thumbnail)
            } else {
                holder.thumbnail.setImageResource(android.R.drawable.ic_media_play)
            }
            holder.itemView.setOnClickListener { onClick(item) }
        } else if (holder is GridViewHolder) {
            holder.title.text = item.title
            holder.duration.text = timeStr
            if (item.isVideo) {
                Glide.with(holder.itemView.context).load(item.path).centerCrop().into(holder.thumbnail)
            } else {
                holder.thumbnail.setImageResource(android.R.drawable.ic_media_play)
            }
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount() = items.size
}
