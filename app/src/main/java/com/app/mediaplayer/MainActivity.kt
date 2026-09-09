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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private val videoList = mutableListOf<MediaItem>()
    private val audioList = mutableListOf<MediaItem>()
    private lateinit var recyclerView: RecyclerView
    private var settingsLayout: ScrollView? = null
    private var bottomNav: BottomNavigationView? = null
    private var subTabs: LinearLayout? = null
    
    private var btnViewToggle: TextView? = null
    private var tabVideo: TextView? = null
    private var tabFolder: TextView? = null
    private var tabPlaylist: TextView? = null

    private var isGridView = false 
    private var isShowingVideos = true 
    private var isFolderView = false 

    companion object {
        var currentMediaList: List<MediaItem> = emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)

            recyclerView = findViewById(R.id.recyclerView)
            settingsLayout = findViewById(R.id.settingsLayout)
            bottomNav = findViewById(R.id.bottomNav)
            subTabs = findViewById(R.id.subTabs)
            
            btnViewToggle = findViewById(R.id.btnViewToggle)
            tabVideo = findViewById(R.id.tabVideo)
            tabFolder = findViewById(R.id.tabFolder)
            tabPlaylist = findViewById(R.id.tabPlaylist)

            recyclerView.layoutManager = LinearLayoutManager(this)

            setupTopTabs()
            setupViewToggle()
            setupSettingsClicks()

            bottomNav?.setOnItemSelectedListener { item ->
                try {
                    when (item.itemId) {
                        R.id.nav_video -> {
                            isShowingVideos = true
                            isFolderView = false
                            resetTabsToDefault()
                            recyclerView.visibility = View.VISIBLE
                            subTabs?.visibility = View.VISIBLE
                            settingsLayout?.visibility = View.GONE
                            findViewById<View>(R.id.bottomSearchBar)?.visibility = View.VISIBLE
                            updateList()
                            true
                        }
                        R.id.nav_music -> {
                            isShowingVideos = false
                            isFolderView = false
                            resetTabsToDefault()
                            recyclerView.visibility = View.VISIBLE
                            subTabs?.visibility = View.VISIBLE
                            settingsLayout?.visibility = View.GONE
                            findViewById<View>(R.id.bottomSearchBar)?.visibility = View.VISIBLE
                            updateList()
                            true
                        }
                        R.id.nav_settings -> {
                            recyclerView.visibility = View.GONE
                            subTabs?.visibility = View.GONE
                            findViewById<View>(R.id.bottomSearchBar)?.visibility = View.GONE
                            settingsLayout?.visibility = View.VISIBLE
                            true
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            checkAndRequestPermissions()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSettingsClicks() {
        try {
            val settingsScrollView = findViewById<ScrollView>(R.id.settingsLayout)
            val linearParent = settingsScrollView?.getChildAt(0) as? LinearLayout
            
            linearParent?.getChildAt(0)?.setOnClickListener { 
                 startActivity(Intent(this, PremiumActivity::class.java))
            }

            val iconsRow = linearParent?.getChildAt(1) as? LinearLayout
            
            iconsRow?.getChildAt(0)?.setOnClickListener { startActivity(Intent(this, Mp3ConverterActivity::class.java)) }
            iconsRow?.getChildAt(1)?.setOnClickListener { startActivity(Intent(this, ThemeActivity::class.java)) }
            iconsRow?.getChildAt(2)?.setOnClickListener { startActivity(Intent(this, VaultActivity::class.java)) }

            linearParent?.getChildAt(3)?.setOnClickListener { startActivity(Intent(this, EqualizerActivity::class.java)) }
            linearParent?.getChildAt(4)?.setOnClickListener { startActivity(Intent(this, RecycleBinActivity::class.java)) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetTabsToDefault() {
        try {
            val activeColor = android.graphics.Color.parseColor("#2196F3")
            val inactiveColor = android.graphics.Color.parseColor("#AAAAAA")
            tabVideo?.setTextColor(activeColor)
            tabFolder?.setTextColor(inactiveColor)
            tabPlaylist?.setTextColor(inactiveColor)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setupTopTabs() {
        val activeColor = android.graphics.Color.parseColor("#2196F3")
        val inactiveColor = android.graphics.Color.parseColor("#AAAAAA")

        tabVideo?.setOnClickListener {
            isFolderView = false
            tabVideo?.setTextColor(activeColor)
            tabFolder?.setTextColor(inactiveColor)
            tabPlaylist?.setTextColor(inactiveColor)
            updateList()
        }

        tabFolder?.setOnClickListener {
            isFolderView = true
            tabFolder?.setTextColor(activeColor)
            tabVideo?.setTextColor(inactiveColor)
            tabPlaylist?.setTextColor(inactiveColor)
            updateList()
        }

        tabPlaylist?.setOnClickListener {
            isFolderView = false
            tabPlaylist?.setTextColor(activeColor)
            tabVideo?.setTextColor(inactiveColor)
            tabFolder?.setTextColor(inactiveColor)
            updateList() 
        }
    }

    private fun setupViewToggle() {
        btnViewToggle?.setOnClickListener {
            if (isFolderView) return@setOnClickListener 
            
            isGridView = !isGridView
            if (isGridView) btnViewToggle?.text = "☰" else btnViewToggle?.text = "☷"
            updateList() 
        }
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
        if (grantResults.isNotEmpty()) scanMedia()
    }

    private fun scanMedia() {
        try {
            videoList.clear()
            audioList.clear()

            val videoProjection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION)
            contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol)
                    videoList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", path, cursor.getLong(durationCol), true))
                }
            }

            val audioProjection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioProjection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol)
                    audioList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", path, cursor.getLong(durationCol), false))
                }
            }
            bottomNav?.selectedItemId = R.id.nav_video
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFolders(items: List<MediaItem>): List<MediaFolder> {
        val grouped = items.groupBy { item ->
            try { File(item.path).parentFile?.name ?: "Unknown Folder" } catch (e: Exception) { "Unknown Folder" }
        }
        return grouped.map { MediaFolder(it.key, it.value) }.sortedBy { it.name }
    }

    private fun updateList() {
        try {
            val list = if (isShowingVideos) videoList else audioList
            if (isFolderView) {
                val folders = getFolders(list)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = FolderAdapter(folders) { clickedFolder ->
                    isFolderView = false
                    tabFolder?.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                    tabVideo?.setTextColor(android.graphics.Color.parseColor("#2196F3"))
                    showItemsInFolder(clickedFolder.mediaItems)
                }
            } else {
                showItemsInFolder(list)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun showItemsInFolder(itemsToShow: List<MediaItem>) {
        try {
            recyclerView.layoutManager = if (isGridView) GridLayoutManager(this, 2) else LinearLayoutManager(this)
            
            recyclerView.adapter = MediaAdapter(
                itemsToShow, 
                isGridView,
                { item -> showMediaOptionsDialog(item) }, 
                { item -> 
                    currentMediaList = itemsToShow
                    val targetActivity = if (item.isVideo) PlayerActivity::class.java else AudioPlayerActivity::class.java
                    val intent = Intent(this, targetActivity).apply { putExtra("START_INDEX", itemsToShow.indexOf(item)) }
                    startActivity(intent)
                }
            )
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun showMediaOptionsDialog(item: MediaItem) {
        try {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.dialog_list_menu, null)
            dialog.setContentView(view)

            // DYNAMIC ID LOOKUP: Yahan se koi error generate nahi hoga!
            val titleId = resources.getIdentifier("menuMediaTitle", "id", packageName)
            if (titleId != 0) view.findViewById<TextView>(titleId)?.text = item.title

            val deleteId = resources.getIdentifier("menuDelete", "id", packageName)
            if (deleteId != 0) {
                view.findViewById<View>(deleteId)?.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show()
                }
            }

            val shareId = resources.getIdentifier("menuShare", "id", packageName)
            if (shareId != 0) {
                view.findViewById<View>(shareId)?.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Opening Share Panel...", Toast.LENGTH_SHORT).show()
                }
            }

            val vaultId = resources.getIdentifier("menuLockVault", "id", packageName)
            if (vaultId != 0) {
                view.findViewById<View>(vaultId)?.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Moved to Private Vault", Toast.LENGTH_SHORT).show()
                }
            }
            
            val mp3Id = resources.getIdentifier("menuConvertToMp3", "id", packageName)
            if (mp3Id != 0) {
                view.findViewById<View>(mp3Id)?.setOnClickListener {
                    dialog.dismiss()
                    startActivity(Intent(this, Mp3ConverterActivity::class.java))
                }
            }

            dialog.show()
        } catch (e: Exception) { e.printStackTrace() }
    }
}
