package com.app.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private var player: Player? = null
    private var mediaController: MediaController? = null 
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

        // Video Scaling (Fit to Screen)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        setupGestures()
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        
        val future = MediaController.Builder(this, sessionToken).buildAsync()
        future.addListener({
            try {
                mediaController = future.get()
                player = mediaController
                playerView.player = player

                // Parcelable error bachane ke liye list yahan se li gayi hai
                val mediaList = MainActivity.currentMediaList
                val startIndex = intent.getIntExtra("START_INDEX", 0)

                if (mediaList.isNotEmpty()) {
                    val isAlreadyPlayingList = player?.mediaItemCount == mediaList.size
                    
                    if (!isAlreadyPlayingList) {
                        val exoItems = mediaList.map { 
                            ExoMediaItem.Builder()
                                .setUri(it.path)
                                .setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).build())
                                .build() 
                        }
                        player?.setMediaItems(exoItems, startIndex, C.TIME_UNSET)
                        player?.prepare()
                        player?.play()
                    } else if (player?.currentMediaItemIndex != startIndex) {
                        player?.seekTo(startIndex, C.TIME_UNSET)
                        player?.play()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onStop() {
        super.onStop()
        mediaController?.release()
        mediaController = null
        player = null
    }

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
                return true
            }

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
}
