package com.app.mediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val videoList = mutableListOf<MediaItem>()
    private val audioList = mutableListOf<MediaItem>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var settingsLayout: ScrollView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var subTabs: LinearLayout

    companion object {
        var currentMediaList: List<MediaItem> = emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        settingsLayout = findViewById(R.id.settingsLayout)
        bottomNav = findViewById(R.id.bottomNav)
        subTabs = findViewById(R.id.subTabs)

        recyclerView.layoutManager = LinearLayoutManager(this)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_video -> {
                    recyclerView.visibility = View.VISIBLE
                    subTabs.visibility = View.VISIBLE
                    settingsLayout.visibility = View.GONE
                    updateList(true)
                    true
                }
                R.id.nav_music -> {
                    recyclerView.visibility = View.VISIBLE
                    subTabs.visibility = View.VISIBLE
                    settingsLayout.visibility = View.GONE
                    updateList(false)
                    true
                }
                R.id.nav_settings -> {
                    recyclerView.visibility = View.GONE
                    subTabs.visibility = View.GONE
                    settingsLayout.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
        } else {
            scanMedia()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            scanMedia()
        }
    }

    private fun scanMedia() {
        videoList.clear()
        audioList.clear()

        val videoProjection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION)
        contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                videoList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", cursor.getString(pathCol), cursor.getLong(durationCol), true))
            }
        }

        val audioProjection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
        contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioProjection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                audioList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", cursor.getString(pathCol), cursor.getLong(durationCol), false))
            }
        }

        bottomNav.selectedItemId = R.id.nav_video
    }

    private fun updateList(showVideos: Boolean) {
        val list = if (showVideos) videoList else audioList
        recyclerView.adapter = MediaAdapter(list) { item ->
            currentMediaList = list
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("START_INDEX", list.indexOf(item))
            }
            startActivity(intent)
        }
    }
}
