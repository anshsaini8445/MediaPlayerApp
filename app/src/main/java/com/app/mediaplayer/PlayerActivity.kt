package com.app.mediaplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var tvGestureStatus: TextView
    private lateinit var audioManager: AudioManager
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        tvGestureStatus = findViewById(R.id.tvGestureStatus)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val mediaPath = intent.getStringExtra("MEDIA_PATH") ?: return finish()

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            val mediaItem = ExoMediaItem.fromUri(Uri.parse(mediaPath))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        setupGestures()
    }

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (e1 == null) return false
                val screenWidth = resources.displayMetrics.widthPixels
                val isRightSide = e1.x > (screenWidth / 2)

                if (abs(distanceY) > abs(distanceX)) {
                    if (isRightSide) {
                        adjustVolume(distanceY)
                    } else {
                        adjustBrightness(distanceY)
                    }
                    return true
                }
                return false
            }
        })

        playerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                tvGestureStatus.visibility = View.GONE
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun adjustVolume(deltaY: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val step = if (deltaY > 0) 1 else -1
        val newVolume = (currentVolume + step).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        showStatus("Volume: ${(newVolume * 100) / maxVolume}%")
    }

    private fun adjustBrightness(deltaY: Float) {
        val layout = window.attributes
        var current = if (layout.screenBrightness < 0) 0.5f else layout.screenBrightness
        current += (deltaY / 1000f)
        layout.screenBrightness = current.coerceIn(0.01f, 1.0f)
        window.attributes = layout
        showStatus("Brightness: ${(layout.screenBrightness * 100).toInt()}%")
    }

    private fun showStatus(text: String) {
        tvGestureStatus.text = text
        tvGestureStatus.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
