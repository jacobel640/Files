package com.example.files.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.StatFs
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import com.example.files.JFileAdapter
import com.example.files.MainActivity.closeAllFragments
import com.example.files.MainActivity.instance
import com.example.files.R
import com.example.files.Statics.TAG_FOLDER
import com.example.files.Statics.dpToPixels
import com.example.files.Statics.openFile
import com.example.files.Statics.openFolder
import com.example.files.presentation.files_explorer.FilesFragment
import com.example.files.utils.FileIcon
import com.example.files.utils.PathFormatter
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.io.File

@SuppressLint("ViewConstructor")
class StorageItem : LinearLayout {
    var itemId: Int = 0
    var file: File
    var documentFile: DocumentFile? = null
    var storageName: String
    var isInternal: Boolean = false
    var isShortcut: Boolean = false
    private val tenDp = dpToPixels(10f)

    constructor(id: Int, file: File, isInternal: Boolean, context: Context) : super(context) {
        this.itemId = id
        this.file = file
        this.isInternal = isInternal
        this.storageName = if (isInternal) {
            context.getString(R.string.internal_storage)
        } else {
            context.getString(R.string.external_storage, (id - 1).toString())
        }
        inflate(context, R.layout.item_storage, this)
        initView()
    }

    constructor(id: Int, file: File, context: Context) : super(context) {
        this.itemId = id
        this.file = file
        this.isShortcut = true
        this.storageName = file.name
        inflate(context, R.layout.item_file_card, this)
        initView()
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    fun initView() {
        findViewById<TextView>(R.id.file_name).text = storageName
        try {
            if (isShortcut) {
                findViewById<TextView>(R.id.file_info).text = PathFormatter(context).format(file.path)
            } else {
                findViewById<TextView>(R.id.file_info).text = getFreeSpace(file)
            }
        } catch (ignored: Exception) {
        }
        if (isInternal) {
            hideDivider()
            findViewById<ImageView>(R.id.type).setImageDrawable(context.getDrawable(R.drawable.phone))
        } else if (!isShortcut) {
            findViewById<ImageView>(R.id.type).setImageDrawable(context.getDrawable(R.drawable.sdcard))
            findViewById<ImageView>(R.id.type).clipToOutline = true
        } else {
            setFavoriteIcon(JFile(file, instance))
        }
        
        if (!context.resources.configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)
            && context.resources.configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)
            && !isShortcut
        ) {
            findViewById<ImageView>(R.id.type).setPadding(tenDp, tenDp / 2, tenDp, tenDp / 2)
        }
        
        findViewById<TextView>(R.id.file_name).textSize = 20f
        findViewById<View>(R.id.item).setOnClickListener {
            if (isShortcut) {
                if (file.isDirectory) openFolder(file)
                else openFile(JFile(file, instance), context)
            } else {
                if (instance?.fragmentInLayout() == true) closeAllFragments()
                instance?.loadFragment(instance?.newFragment(file), TAG_FOLDER)
            }
        }
        if (isShortcut) {
            findViewById<View>(R.id.item).setOnLongClickListener {
                instance?.loadFragment(FilesFragment.newInstance("FAVORITES", null, null), "favorites")
                true
            }
        }
    }

    fun setFavoriteIcon(jFile: JFile) {
        val viewHolder = ViewHolder(this)
        FileIcon.setIcon(viewHolder, JFileAdapter.ViewType.ROW, jFile, instance)
        viewHolder.size.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    fun updateView() {
        try {
            if (!isShortcut) findViewById<TextView>(R.id.file_info).text = getFreeSpace(file)
        } catch (ignored: Exception) {
        }
    }

    fun getFreeSpace(file: File): String {
        val stat = StatFs(file.path)
        val availBlocks = stat.availableBlocksLong.toDouble()
        val blockSize = stat.blockSizeLong.toDouble()
        val freeMemory = (availBlocks * blockSize).toLong()
        val totalMemory = stat.totalBytes

        var free = Math.toIntExact(freeMemory / 1000000000)
        var total = Math.toIntExact(totalMemory / 1000000000)
        val percent = total.toDouble() / 100

        total = (total / percent).toInt()
        free = (free / percent).toInt()

        val progress = total - free

        findViewById<LinearProgressIndicator>(R.id.capacity).progress = progress

        Log.d("##### setProgress #####", "t:$total, f:$free, p:$percent | t-f=${total - free}")

        return "${freeMemory / 1000000000} ${context.getString(R.string.gb)} / ${totalMemory / 1000000000} ${context.getString(R.string.gb)}"
    }

    fun hideDivider() {
        findViewById<View>(R.id.divider).visibility = View.GONE
    }
}
