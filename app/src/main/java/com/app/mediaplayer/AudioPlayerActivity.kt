package com.app.mediaplayer

import android.content.ComponentName
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private var player: Player? = null
    private var mediaController: MediaController? = null
    private lateinit var tvTitle: TextView
    private lateinit var tvCurrent: TextView
    private lateinit var tvTotal: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            player?.let {
                tvCurrent.text = formatTime(it.currentPosition)
                seekBar.progress = it.currentPosition.toInt()
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        tvTitle = findViewById(R.id.tvAudioTitle)
        tvCurrent = findViewById(R.id.tvAudioCurrent)
        tvTotal = findViewById(R.id.tvAudioTotal)
        seekBar = findViewById(R.id.seekAudio)
        btnPlayPause = findViewById(R.id.btnAudioPlayPause)

        findViewById<ImageButton>(R.id.btnBackAudio).setOnClickListener { finish() }
        
        findViewById<TextView>(R.id.btnEqAudio).setOnClickListener { openEqualizer() }
        findViewById<TextView>(R.id.btnShuffleAudio).setOnClickListener { Toast.makeText(this, "Shuffle Mode", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnTimerAudio).setOnClickListener { Toast.makeText(this, "Sleep Timer Set", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnRepeatAudio).setOnClickListener { Toast.makeText(this, "Repeat Mode", Toast.LENGTH_SHORT).show() }
        findViewById<TextView>(R.id.btnPlaylistAudio).setOnClickListener { Toast.makeText(this, "Opening Playlist", Toast.LENGTH_SHORT).show() }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, sessionToken).buildAsync()
        
        future.addListener({
            mediaController = future.get()
            player = mediaController
            setupPlayer()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupPlayer() {
        val mediaList = MainActivity.currentMediaList
        val startIndex = intent.getIntExtra("START_INDEX", 0)

        if (mediaList.isNotEmpty()) {
            if (player?.mediaItemCount != mediaList.size) {
                val exoItems = mediaList.map { 
                    MediaItem.Builder()
                        .setUri(it.path)
                        .setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).build())
                        .build() 
                }
                player?.setMediaItems(exoItems, startIndex, 0L)
                player?.prepare()
                player?.play()
            } else if (player?.currentMediaItemIndex != startIndex) {
                player?.seekTo(startIndex, 0L)
                player?.play()
            }
        }

        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                tvTitle.text = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Audio"
                player?.let {
                    seekBar.max = it.duration.toInt()
                    tvTotal.text = formatTime(it.duration)
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                    handler.post(updateProgressRunnable)
                } else {
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                    handler.removeCallbacks(updateProgressRunnable)
                }
            }
        })

        btnPlayPause.setOnClickListener {
            if (player?.isPlaying == true) player?.pause() else player?.play()
        }

        findViewById<ImageButton>(R.id.btnAudioPrev).setOnClickListener { player?.seekToPreviousMediaItem() }
        findViewById<ImageButton>(R.id.btnAudioNext).setOnClickListener { player?.seekToNextMediaItem() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player?.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun openEqualizer() {
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        }
        try { startActivityForResult(intent, 0) } catch (e: Exception) { Toast.makeText(this, "Equalizer not supported", Toast.LENGTH_SHORT).show() }
    }

    private fun formatTime(ms: Long): String {
        if (ms < 0) return "00:00"
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateProgressRunnable)
        mediaController?.release()
    }
}
