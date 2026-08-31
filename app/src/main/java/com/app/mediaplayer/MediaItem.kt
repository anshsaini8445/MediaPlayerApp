package com.app.mediaplayer

data class MediaItem(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val isVideo: Boolean
)
