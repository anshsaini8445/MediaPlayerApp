package com.app.mediaplayer

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PlayerActivity : AppCompatActivity() {

    private var player: Player? = null
    private var mediaController: MediaController? = null 
    private lateinit var playerView: PlayerView
    private lateinit var tvGestureStatus: TextView
    private var btnAudioOnly: LinearLayout? = null
    private lateinit var audioManager: AudioManager
    
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    private var isLocked = false
    private var currentSpeed = 1.0f
    private var resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
    private var isMirrored = false 
    private var isAudioOnlyMode = false
    private var isHWDecoder = true // HW / SW Decoder track karne ke liye

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        tvGestureStatus = findViewById(R.id.tvGestureStatus)
        btnAudioOnly = findViewById(R.id.btnAudioOnly)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        playerView.resizeMode = resizeMode
        
        setupGestures()
        setupAudioOnlyButton()
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

                setupCustomControls()

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

    private fun setupAudioOnlyButton() {
        btnAudioOnly?.setOnClickListener {
            isAudioOnlyMode = !isAudioOnlyMode
            if (isAudioOnlyMode) {
                playerView.visibility = View.INVISIBLE
                Toast.makeText(this, "Audio Mode Enabled 🎧", Toast.LENGTH_SHORT).show()
            } else {
                playerView.visibility = View.VISIBLE
                Toast.makeText(this, "Video Mode Enabled 🎬", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCustomControls() {
        val tvTitle = playerView.findViewById<TextView>(R.id.tvVideoTitle)
        val btnBack = playerView.findViewById<ImageButton>(R.id.btnBack)
        val btnPiP = playerView.findViewById<ImageButton>(R.id.btnPiP)
        val btnMute = playerView.findViewById<ImageButton>(R.id.btnMute)
        val btnLock = playerView.findViewById<ImageButton>(R.id.btnLock)
        val tvSpeed = playerView.findViewById<TextView>(R.id.tvSpeed)
        val btnResize = playerView.findViewById<ImageButton>(R.id.btnResize)
        val btnMoreSettings = playerView.findViewById<ImageButton>(R.id.btnMoreSettings)
        
        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                tvTitle.text = mediaItem?.mediaMetadata?.title?.toString() ?: "MAX Player Video"
            }
        })

        btnBack.setOnClickListener { finish() }

        btnPiP.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build())
            }
        }

        btnMute.setOnClickListener {
            player?.let { p ->
                if (p.volume > 0f) {
                    p.volume = 0f
                    Toast.makeText(this, "Video Muted", Toast.LENGTH_SHORT).show()
                } else {
                    p.volume = 1f
                    Toast.makeText(this, "Volume Restored", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvSpeed.setOnClickListener {
            currentSpeed = when (currentSpeed) {
                1.0f -> 1.5f
                1.5f -> 2.0f
                2.0f -> 0.5f
                else -> 1.0f
            }
            player?.playbackParameters = PlaybackParameters(currentSpeed)
            tvSpeed.text = "${currentSpeed}x"
            Toast.makeText(this, "Speed: ${currentSpeed}x", Toast.LENGTH_SHORT).show()
        }

        btnResize.setOnClickListener {
            resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            playerView.resizeMode = resizeMode
        }

        btnLock.setOnClickListener {
            isLocked = !isLocked
            if(isLocked) {
                Toast.makeText(this, "Screen Locked \uD83D\uDD12", Toast.LENGTH_SHORT).show()
                playerView.findViewById<View>(R.id.layoutTopControls).visibility = View.INVISIBLE
                playerView.findViewById<View>(R.id.layoutBottomControls).visibility = View.INVISIBLE
                playerView.findViewById<View>(R.id.btnMute).visibility = View.INVISIBLE
                playerView.findViewById<View>(R.id.btnCut).visibility = View.INVISIBLE
                playerView.findViewById<View>(R.id.btnScreenshot).visibility = View.INVISIBLE
            } else {
                Toast.makeText(this, "Screen Unlocked \uD83D\uDD13", Toast.LENGTH_SHORT).show()
                playerView.findViewById<View>(R.id.layoutTopControls).visibility = View.VISIBLE
                playerView.findViewById<View>(R.id.layoutBottomControls).visibility = View.VISIBLE
                playerView.findViewById<View>(R.id.btnMute).visibility = View.VISIBLE
                playerView.findViewById<View>(R.id.btnCut).visibility = View.VISIBLE
                playerView.findViewById<View>(R.id.btnScreenshot).visibility = View.VISIBLE
            }
        }

        // 3 Dots (More Settings) par Naya Premium Pop-up kholna
        btnMoreSettings.setOnClickListener { 
            showAdvancedSettingsDialog()
        }

        playerView.findViewById<ImageButton>(R.id.btnCut).setOnClickListener { 
            Toast.makeText(this, "Video Cutter Mode", Toast.LENGTH_SHORT).show() 
        }
        playerView.findViewById<ImageButton>(R.id.btnAudioTrack).setOnClickListener { 
            Toast.makeText(this, "Audio Track Selector", Toast.LENGTH_SHORT).show() 
        }
        playerView.findViewById<ImageButton>(R.id.btnScreenshot).setOnClickListener { 
            Toast.makeText(this, "Screenshot Captured! \uD83D\uDCF8", Toast.LENGTH_SHORT).show() 
        }
    }

    // NAYA PREMIUM SETTINGS MENU
    private fun showAdvancedSettingsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_more_settings, null)
        dialog.setContentView(view)

        // Row 1
        view.findViewById<View>(R.id.btnAudioTrackMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Audio Track Options", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnEqualizerMenu).setOnClickListener { dialog.dismiss(); openEqualizer() }
        view.findViewById<View>(R.id.btnCastMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Searching for TV to Cast 📺...", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnShareMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Share Menu", Toast.LENGTH_SHORT).show() }

        // Row 2
        view.findViewById<View>(R.id.btnCutMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Video Cutter Mode", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnFavMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Added to Favorites ⭐", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnBookmarkMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Video Bookmarked 🔖", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnVRMenu).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "VR 360 Mode Activated 🕶️", Toast.LENGTH_SHORT).show() }

        // Settings Row
        view.findViewById<View>(R.id.btnABRepeat).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "A-B Repeat Mode", Toast.LENGTH_SHORT).show() }
        
        view.findViewById<View>(R.id.btnNightMode).setOnClickListener {
            dialog.dismiss()
            val layout = window.attributes
            layout.screenBrightness = 0.05f 
            window.attributes = layout
            Toast.makeText(this, "Night Mode ON 🌙", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnMirrorMode).setOnClickListener {
            dialog.dismiss()
            isMirrored = !isMirrored
            val videoSurface = playerView.videoSurfaceView
            if (videoSurface != null) {
                videoSurface.scaleX = if (isMirrored) -1f else 1f
                Toast.makeText(this, if (isMirrored) "Mirror Mode ON ◨◧" else "Mirror Mode OFF", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<View>(R.id.btnTimer).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Sleep Timer Set ⏱️", Toast.LENGTH_SHORT).show() }

        // Decoder Toggle
        val btnHW = view.findViewById<TextView>(R.id.btnHW)
        val btnSW = view.findViewById<TextView>(R.id.btnSW)
        
        // Setup initial colors based on current state
        if(isHWDecoder) {
            btnHW.setTextColor(android.graphics.Color.parseColor("#00E676"))
            btnSW.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
        } else {
            btnHW.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            btnSW.setTextColor(android.graphics.Color.parseColor("#00E676"))
        }

        btnHW.setOnClickListener {
            isHWDecoder = true
            Toast.makeText(this, "Switched to Hardware Decoder", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnSW.setOnClickListener {
            isHWDecoder = false
            Toast.makeText(this, "Switched to Software Decoder", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // More Row
        view.findViewById<View>(R.id.btnEditInfo).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Edit Video Info", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnTutorial).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Opening Tutorials...", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnFileInfo).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Showing File Info", Toast.LENGTH_SHORT).show() }
        view.findViewById<View>(R.id.btnFeedback).setOnClickListener { dialog.dismiss(); Toast.makeText(this, "Send Feedback", Toast.LENGTH_SHORT).show() }

        dialog.show()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isAudioOnlyMode) {
            enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build())
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        playerView.useController = !isInPictureInPictureMode
        btnAudioOnly?.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        mediaController?.release()
        mediaController = null
        player = null
    }

    private fun setupGestures() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (isLocked || isAudioOnlyMode) return false 
                
                scaleFactor *= detector.scaleFactor
                scaleFactor = max(1.0f, min(scaleFactor, 6.0f)) 
                val contentFrame = playerView.findViewById<View>(androidx.media3.ui.R.id.exo_content_frame)
                contentFrame?.scaleX = scaleFactor
                contentFrame?.scaleY = scaleFactor
                return true
            }
        })

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (isLocked) return false 
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (isLocked) return 
                openEqualizer()
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (isLocked || e1 == null) return false 
                
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
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                tvGestureStatus.visibility = View.GONE
            }
            false 
        }
    }

    private fun openEqualizer() {
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE)
        }
        try {
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            showStatus("Equalizer not supported on this device")
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
