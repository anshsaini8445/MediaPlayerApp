package com.app.mediaplayer

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var tvTitle: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    
    private var tapCount = 0
    private var lastTapTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        tvTitle = findViewById(R.id.tvAudioTitle)
        seekBar = findViewById(R.id.seekAudio)
        btnPlayPause = findViewById(R.id.btnAudioPlayPause)

        setupPlayer()

        // 3-Tap Logic for Premium Page
        findViewById<RelativeLayout>(R.id.audioRootView).setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime < 600) {
                tapCount++
                if (tapCount == 3) {
                    tapCount = 0
                    startActivity(Intent(this, PremiumActivity::class.java))
                }
            } else {
                tapCount = 1
            }
            lastTapTime = currentTime
        }

        findViewById<TextView>(R.id.btnTimerAudio).setOnClickListener { Toast.makeText(this, "Sleep Timer", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnShuffleAudio).setOnClickListener { Toast.makeText(this, "Shuffle Mode", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnPlaylistAudio).setOnClickListener { Toast.makeText(this, "Playlist", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnMenuAudio).setOnClickListener { Toast.makeText(this, "Options Menu", Toast.LENGTH_SHORT).show() }

        btnPlayPause.setOnClickListener {
            if (player?.isPlaying == true) player?.pause() else player?.play()
            updatePlayButton()
        }
        findViewById<ImageButton>(R.id.btnAudioPrev).setOnClickListener { player?.seekToPreviousMediaItem() }
        findViewById<ImageButton>(R.id.btnAudioNext).setOnClickListener { player?.seekToNextMediaItem() }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        val mediaList = MainActivity.currentMediaList
        val startIndex = intent.getIntExtra("START_INDEX", 0)

        if (mediaList.isNotEmpty()) {
            val exoItems = mediaList.map {
                MediaItem.Builder()
                    .setUri(it.path)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).build())
                    .build()
            }
            player?.setMediaItems(exoItems, startIndex, 0L)
            player?.prepare()
            player?.play()
            updatePlayButton()
        }

        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                tvTitle.text = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Audio"
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayButton()
            }
        })
    }

    private fun updatePlayButton() {
        if (player?.isPlaying == true) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
