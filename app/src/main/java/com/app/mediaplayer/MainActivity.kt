package com.app.mediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
    private lateinit var settingsLayout: ScrollView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var subTabs: LinearLayout
    private lateinit var btnViewToggle: TextView
    private lateinit var tabVideo: TextView
    private lateinit var tabFolder: TextView
    private lateinit var tabPlaylist: TextView
    private lateinit var searchBar: EditText

    private var isGridView = false
    private var isShowingVideos = true
    private var isFolderView = false

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
        btnViewToggle = findViewById(R.id.btnViewToggle)
        tabVideo = findViewById(R.id.tabVideo)
        tabFolder = findViewById(R.id.tabFolder)
        tabPlaylist = findViewById(R.id.tabPlaylist)
        searchBar = findViewById(R.id.bottomSearchBar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupTopTabs()
        setupViewToggle()
        setupSettingsClicks()
        setupSearchBar()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_video -> {
                    isShowingVideos = true
                    isFolderView = false
                    resetTabsToDefault()
                    recyclerView.visibility = View.VISIBLE
                    subTabs.visibility = View.VISIBLE
                    settingsLayout.visibility = View.GONE
                    findViewById<View>(R.id.bottomSearchBarContainer)?.visibility = View.VISIBLE
                    updateList()
                    true
                }
                R.id.nav_music -> {
                    isShowingVideos = false
                    isFolderView = false
                    resetTabsToDefault()
                    recyclerView.visibility = View.VISIBLE
                    subTabs.visibility = View.VISIBLE
                    settingsLayout.visibility = View.GONE
                    findViewById<View>(R.id.bottomSearchBarContainer)?.visibility = View.VISIBLE
                    updateList()
                    true
                }
                R.id.nav_settings -> {
                    recyclerView.visibility = View.GONE
                    subTabs.visibility = View.GONE
                    findViewById<View>(R.id.bottomSearchBarContainer)?.visibility = View.GONE
                    settingsLayout.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
        checkAndRequestPermissions()
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    updateList()
                } else {
                    val baseList = if (isShowingVideos) videoList else audioList
                    val filteredList = baseList.filter { it.title.contains(query, ignoreCase = true) }
                    showItemsInFolder(filteredList)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSettingsClicks() {
        val settingsScrollView = findViewById<ScrollView>(R.id.settingsLayout)
        val linearParent = settingsScrollView.getChildAt(0) as LinearLayout

        // Premium Banner
        linearParent.getChildAt(0).setOnClickListener { startActivity(Intent(this, PremiumActivity::class.java)) }

        // Icons Row (MP3, Theme, Vault)
        val iconsRow = linearParent.getChildAt(1) as LinearLayout
        iconsRow.getChildAt(0).setOnClickListener { startActivity(Intent(this, Mp3ConverterActivity::class.java)) }
        iconsRow.getChildAt(1).setOnClickListener { startActivity(Intent(this, ThemeActivity::class.java)) }
        iconsRow.getChildAt(2).setOnClickListener { startActivity(Intent(this, VaultActivity::class.java)) }

        // Equalizer & Recycle Bin
        linearParent.getChildAt(2).setOnClickListener { startActivity(Intent(this, EqualizerActivity::class.java)) }
        linearParent.getChildAt(3).setOnClickListener { startActivity(Intent(this, RecycleBinActivity::class.java)) }
    }

    private fun resetTabsToDefault() {
        val activeColor = android.graphics.Color.parseColor("#00E5FF")
        val inactiveColor = android.graphics.Color.parseColor("#AAAAAA")
        tabVideo.setTextColor(activeColor)
        tabFolder.setTextColor(inactiveColor)
        tabPlaylist.setTextColor(inactiveColor)
    }

    private fun setupTopTabs() {
        val activeColor = android.graphics.Color.parseColor("#00E5FF")
        val inactiveColor = android.graphics.Color.parseColor("#AAAAAA")

        tabVideo.setOnClickListener {
            isFolderView = false
            tabVideo.setTextColor(activeColor)
            tabFolder.setTextColor(inactiveColor)
            tabPlaylist.setTextColor(inactiveColor)
            updateList()
        }
        tabFolder.setOnClickListener {
            isFolderView = true
            tabFolder.setTextColor(activeColor)
            tabVideo.setTextColor(inactiveColor)
            tabPlaylist.setTextColor(inactiveColor)
            updateList()
        }
        tabPlaylist.setOnClickListener {
            isFolderView = false
            tabPlaylist.setTextColor(activeColor)
            tabVideo.setTextColor(inactiveColor)
            tabFolder.setTextColor(inactiveColor)
            updateList()
        }
    }

    private fun setupViewToggle() {
        btnViewToggle.setOnClickListener {
            if (isFolderView) return@setOnClickListener
            isGridView = !isGridView
            if (isGridView) btnViewToggle.text = "▦" else btnViewToggle.text = "☰"
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
        videoList.clear()
        audioList.clear()

        val videoProjection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA)
        contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoProjection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            
            while (cursor.moveToNext()) {
                videoList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", cursor.getString(pathCol), true))
            }
        }

        val audioProjection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA)
        contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioProjection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            
            while (cursor.moveToNext()) {
                audioList.add(MediaItem(cursor.getLong(idCol), cursor.getString(titleCol) ?: "Unknown", cursor.getString(pathCol), false))
            }
        }
        bottomNav.selectedItemId = R.id.nav_video
    }

    private fun getFolders(items: List<MediaItem>): List<MediaFolder> {
        val grouped = items.groupBy { item ->
            try { File(item.path).parentFile?.name ?: "Unknown Folder" } catch (e: Exception) { "Unknown Folder" }
        }
        return grouped.map { MediaFolder(it.key, it.value) }.sortedBy { it.name }
    }

    private fun updateList() {
        val list = if (isShowingVideos) videoList else audioList
        if (isFolderView) {
            val folders = getFolders(list)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = FolderAdapter(folders) { clickedFolder ->
                isFolderView = false
                tabFolder.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                tabVideo.setTextColor(android.graphics.Color.parseColor("#00E5FF"))
                showItemsInFolder(clickedFolder.mediaItems)
            }
        } else {
            showItemsInFolder(list)
        }
    }

    private fun showItemsInFolder(itemsToShow: List<MediaItem>) {
        recyclerView.layoutManager = if (isGridView) GridLayoutManager(this, 2) else LinearLayoutManager(this)
        
        recyclerView.adapter = MediaAdapter(itemsToShow, isGridView) { item ->
            currentMediaList = itemsToShow
            val targetActivity = if (item.isVideo) PlayerActivity::class.java else AudioPlayerActivity::class.java
            val intent = Intent(this, targetActivity).apply { putExtra("START_INDEX", itemsToShow.indexOf(item)) }
            startActivity(intent)
        }

        // Long Press on any item to open the 3-Dot Menu options
        recyclerView.clearOnChildAttachStateChangeListeners()
        recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnLongClickListener {
                    val position = recyclerView.getChildAdapterPosition(view)
                    if (position in itemsToShow.indices) {
                        showMediaOptionsDialog(itemsToShow[position])
                    }
                    true
                }
            }
            override fun onChildViewDetachedFromWindow(view: View) {
                view.setOnLongClickListener(null)
            }
        })
    }

    private fun showMediaOptionsDialog(item: MediaItem) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_list_menu, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.menuMediaTitle).text = item.title

        if (item.isVideo) {
            view.findViewById<View>(R.id.menuSetRingtone).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.menuConvertMp3).visibility = View.GONE
            view.findViewById<View>(R.id.menuProperties).visibility = View.GONE
        }

        view.findViewById<View>(R.id.menuDelete).setOnClickListener {
            dialog.dismiss()
            val mediaType = if (item.isVideo) "video" else "audio"
            android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Delete File")
                .setMessage("Want to delete this $mediaType?")
                .setPositiveButton("Delete") { _, _ ->
                    // UI Toast. Actual file deletion logic needs scoped storage permission
                    android.widget.Toast.makeText(this, "File sent to Recycle Bin", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<View>(R.id.menuShare).setOnClickListener {
            dialog.dismiss()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (item.isVideo) "video/*" else "audio/*"
                putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(item.path))
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        view.findViewById<View>(R.id.menuLockVault).setOnClickListener {
            dialog.dismiss()
            android.widget.Toast.makeText(this, "Moved to Private Vault", android.widget.Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.menuConvertMp3).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, Mp3ConverterActivity::class.java))
        }

        dialog.show()
    }
}
