package com.app.mediaplayer

data class MediaItem(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long = 0L,
    val isVideo: Boolean
)

data class MediaFolder(
    val name: String,
    val mediaItems: List<MediaItem>
)
