package com.app.mediaplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

data class MediaItem(val id: Long, val title: String, val path: String, val duration: Long, val isVideo: Boolean)
data class MediaFolder(val name: String, val mediaItems: List<MediaItem>)

class MediaAdapter(
    private val items: List<MediaItem>,
    private val isGrid: Boolean,
    private val onMoreClick: (MediaItem) -> Unit,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    // Background Thread taaki list scroll karte waqt phone hang na ho
    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        
        val totalSecs = item.duration / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        holder.tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        try {
            val file = File(item.path)
            val sizeMb = file.length() / (1024 * 1024)
            holder.tvSubtitle.text = "${sizeMb} MB  •  ${file.parentFile?.name ?: "Unknown"}"
        } catch (e: Exception) {
            holder.tvSubtitle.text = "Unknown Size"
        }

        // Default Icon set karna
        if (item.isVideo) {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_media_play)
        } else {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_media_ff) 
        }

        // Tag set karna zaroori hai warna scroll karte waqt photo mix ho jayengi
        holder.imgThumbnail.tag = item.path

        // Background mein asli Photo (Thumbnail) nikalna
        executor.execute {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(item.path)
                val bitmap = if (item.isVideo) {
                    retriever.getFrameAtTime(1000000) // 1 second aage ki photo
                } else {
                    val art = retriever.embeddedPicture
                    if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
                }
                retriever.release()

                if (bitmap != null && holder.imgThumbnail.tag == item.path) {
                    mainHandler.post {
                        holder.imgThumbnail.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                // Ignore corrupt files
            }
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
