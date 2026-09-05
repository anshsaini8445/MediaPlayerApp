package com.app.mediaplayer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.abs

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var tvNanoOverlay: TextView? = null
    private lateinit var gestureDetector: GestureDetector

    private var isSeeking = false
    private var seekPosition: Long = 0
    private var totalDuration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)

        val btnBack = playerView.findViewById<ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener { finish() }

        val btnScreenshot = playerView.findViewById<ImageButton>(R.id.btnScreenshot)
        btnScreenshot?.setOnClickListener { ScreenshotHelper.captureFrame(this, playerView) }

        // NAYA: PLAYit Style Menu open karne ka code
        val btnMoreSettings = playerView.findViewById<ImageButton>(R.id.btnMoreSettings)
        btnMoreSettings?.setOnClickListener {
            showPlayitStyleMenu()
        }

        tvNanoOverlay = playerView.findViewById(R.id.tvNanoSecondOverlay)

        initializePlayer()
        setupSwipeGestures()
    }

    private fun showPlayitStyleMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_video_settings, null)
        dialog.setContentView(view)
        
        // Background ko transparent banana taaki rounded corners aur design sahi dikhe
        (view.parent as View).setBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        dialog.show()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

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
        }

        val tvTitle = playerView.findViewById<TextView>(R.id.tvVideoTitle)
        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                tvTitle?.text = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Video"
                totalDuration = player?.duration ?: 0
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (e1 == null || totalDuration <= 0) return false

                if (abs(distanceX) > abs(distanceY)) {
                    isSeeking = true
                    tvNanoOverlay?.visibility = View.VISIBLE
                    
                    val change = (distanceX * -100).toLong() 
                    seekPosition = player?.currentPosition ?: 0
                    seekPosition += change
                    
                    if (seekPosition < 0) seekPosition = 0
                    if (seekPosition > totalDuration) seekPosition = totalDuration
                    
                    tvNanoOverlay?.text = PrecisionTimeFormatter.formatWithMillis(seekPosition)
                    return true
                }
                return false
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            
            if (event.action == MotionEvent.ACTION_UP && isSeeking) {
                player?.seekTo(seekPosition)
                tvNanoOverlay?.visibility = View.GONE
                isSeeking = false
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
