package com.example.files.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.icu.text.Collator
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.files.R
import com.example.files.Statics
import com.example.files.listeners.OnIconLoadReady
import com.example.files.listeners.OnSizeLoadReady
import com.example.files.utils.FileIcon
import com.example.files.utils.JFileExecutor
import com.example.files.utils.PathFormatter
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class JFile : File, Comparable<File> {

    enum class Type { FOLDER, IMAGE, VIDEO, AUDIO, APK, ARCHIVE, DOCUMENT, SHORTCUT, OTHER }

    data class CachedSize(val size: Long, val lastModified: Long)

    companion object {
        val sizeCache = ConcurrentHashMap<String, CachedSize>()
        
        @JvmStatic
        fun nullSize(list: Array<out Any>?): Int {
            return list?.size ?: -1
        }
    }

    var id: String? = null
    lateinit var context: Context
    var info: CharSequence = ""
        get() = DateFormat.format("HH:mm dd/MM/yyyy", Date(lastModified()))
    @get:JvmName("isSelected") var selected: Boolean = false
    var type: Type
    var position: Int = 0
    
    private var sizeLoadListener: OnSizeLoadReady? = null
    @Volatile var isSizeLoading: Boolean = false
        private set
    @Volatile private var _size: Long = -1
    private var count: Long = 0
    private var iconLoadListener: OnIconLoadReady? = null
    var lastChecked: Long = 0
        private set
    @Volatile var cachedIcon: Any? = null
    @Volatile private var iconLoading: Boolean = false

    constructor(path: String, context: Context) : super(path) {
        this.context = context
        this.type = FileIcon.types(myGetExtension().lowercase(), isDirectory)
        initCache()
    }

    constructor(id: String, path: String, context: Context) : super(path) {
        this.id = id
        this.context = context
        this.type = FileIcon.types(myGetExtension().lowercase(), isDirectory)
        initCache()
    }

    constructor(file: File, context: Context) : super(file.path) {
        this.context = context
        this.type = FileIcon.types(myGetExtension().lowercase(), isDirectory)
        initCache()
    }

    constructor(pathname: String) : super(pathname) {
        this.type = FileIcon.types(myGetExtension().lowercase(), isDirectory)
    }

    private fun initCache() {
        sizeCache[path]?.let { cached ->
            if (cached.lastModified == lastModified()) {
                this._size = cached.size
                this.lastChecked = cached.lastModified
            }
        }
    }

    fun fromFile(): DocumentFile? = DocumentFile.fromFile(this)

    fun getDocumentFileOrig(): DocumentFile? {
        val uri = FileProvider.getUriForFile(context, context.packageName, this)
        return DocumentFile.fromSingleUri(context, uri)
    }

    fun getDocumentFile(): DocumentFile? {
        val uriStr = PathFormatter(context).externalFilePathWoName(
            FileProvider.getUriForFile(context, context.packageName, this).toString()
        )
        return DocumentFile.fromSingleUri(context, Uri.parse(uriStr))
    }

    fun getDocumentTree(): DocumentFile? {
        val uriStr = PathFormatter(context).externalFilePathWoName(
            FileProvider.getUriForFile(context, context.packageName, this).toString()
        )
        return DocumentFile.fromTreeUri(context, Uri.parse(uriStr))
    }

    fun getDocumentTreeSec(): DocumentFile? {
        val uri = Uri.parse(PathFormatter(context).externalFolderPathWoName(this.path))
        return DocumentFile.fromTreeUri(context, uri)
    }

    fun getUri(): Uri? = getDocumentFile()?.uri

    fun isDocumentFile(): Boolean = Statics.isDocumentFile(this) && !Statics.isRootFile(this)

    fun listJFiles(): Array<JFile>? {
        return listFiles()?.map { JFile(it, context) }?.toTypedArray()
    }

    val stringDate: String get() {
        return DateUtils.getRelativeTimeSpanString(
            lastModified(),
            Calendar.getInstance().timeInMillis,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    val stringSize: String get() {
        return if (isDirectory) {
            if (Statics.showFileSize) {
                if (isSizeReady) {
                    Formatter.formatFileSize(context, size)
                } else {
                    loadSizeIfNeeded()
                    context.getString(R.string.loading)
                }
            } else {
                countItems
            }
        } else {
            Formatter.formatFileSize(context, size)
        }
    }

    val countItems: String get() {
        return if (isDirectory) {
            if (count == 0L) count = countFiles().toLong()
            context.getString(R.string.items, count.toString())
        } else {
            Formatter.formatFileSize(context, length())
        }
    }

    fun countFiles(): Int = nullSize(list())

    val size: Long get() {
        return if (isFile) length()
        else if (Statics.showFileSize) _size
        else nullSize(listJFiles()).toLong()
    }

    val isSizeReady: Boolean get() {
        if (!isDirectory) return true
        if (lastChecked == 0L) return false
        return !Date(lastModified()).after(Date(lastChecked))
    }

    fun loadSizeIfNeeded() {
        if (!isDirectory) {
            _size = length()
            return
        }
        if (isSizeLoading || isSizeReady) return
        isSizeLoading = true

        JFileExecutor.execute {
            val runningTotal = LongArray(1) { 0L }
            val lastUpdate = LongArray(1) { System.currentTimeMillis() }
            calculateFolderSize(this, runningTotal, lastUpdate)
            val result = runningTotal[0]

            _size = result
            lastChecked = lastModified()
            sizeCache[path] = CachedSize(result, lastChecked)
            isSizeLoading = false

            sizeLoadListener?.onSizeReady(result)
        }
    }

    private fun calculateFolderSize(dir: File, runningTotal: LongArray, lastUpdate: LongArray) {
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                calculateFolderSize(f, runningTotal, lastUpdate)
            } else {
                runningTotal[0] += f.length()
                if (System.currentTimeMillis() - lastUpdate[0] > 100) {
                    this._size = runningTotal[0]
                    sizeLoadListener?.onSizeUpdate(runningTotal[0])
                    lastUpdate[0] = System.currentTimeMillis()
                }
            }
        }
    }

    fun setSizeLoadListener(listener: OnSizeLoadReady?) {
        this.sizeLoadListener = listener
    }

    val isIconReady: Boolean get() = cachedIcon != null

    fun loadIconIfNeeded() {
        if (cachedIcon != null || iconLoading) return
        iconLoading = true

        JFileExecutor.execute {
            val icon = loadIconInternal()
            cachedIcon = icon
            iconLoading = false
            iconLoadListener?.onIconReady(icon)
        }
    }

    fun setIconReadyListener(listener: OnIconLoadReady?) {
        this.iconLoadListener = listener
    }

    @SuppressLint("UseCompatLoadingForDrawables", "StaticFieldLeak")
    fun loadIconInternal(): Any {
        if (isDirectory) return R.drawable.folder
        
        return when (myGetExtension().lowercase()) {
            "aac", "amr", "flac", "mp3", "m4a", "ogg", "opus", "wma", "wav" -> {
                try {
                    MediaMetadataRetriever().use { mmr ->
                        mmr.setDataSource(path)
                        mmr.embeddedPicture?.let { return it }
                    }
                } catch (ignored: Exception) {}
                R.drawable.ctg_audio
            }
            "apk" -> {
                try {
                    val packageInfo = context.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
                    if (packageInfo != null) {
                        val appInfo = packageInfo.applicationInfo
                        appInfo?.sourceDir = path
                        appInfo?.publicSourceDir = path
                        return appInfo?.loadIcon(context.packageManager) ?: R.drawable.ext_apk
                    }
                } catch (ignored: Exception) {}
                R.drawable.ext_apk
            }
            "cr2", "dng", "heic", "jpg", "jpeg", "png", "raw", "webp", "ico",
            "3gpp", "avi", "gif", "mkv", "mov", "mp4" -> Uri.fromFile(this)
            
            "7z", "7zip", "apks", "apkm", "xapk", "gz", "jar", "rar", "zip" -> R.drawable.ctg_archive
            "docx", "doc" -> R.drawable.ext_word
            "xls", "xlsx" -> R.drawable.ext_excel
            "pptx" -> R.drawable.ext_powerpoint
            "pdf" -> R.drawable.ext_pdf
            "txt" -> R.drawable.ext_txt
            "html" -> backgroundDrawable()
            else -> R.drawable.file
        }
    }

    private fun backgroundDrawable(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
            setColor(Color.BLUE)
        }
    }

    fun getExtension(): String = myGetExtension()

    private fun myGetExtension(): String {
        if (isDirectory) return "folder"
        val fileName = name
        val lastIndex = fileName.lastIndexOf(".")
        return if (lastIndex != -1) fileName.substring(lastIndex + 1) else ""
    }

    val nameTLC: String get() = name.lowercase()

    override fun toString(): String = path

    override fun compareTo(other: File): Int {
        val collator = Collator.getInstance(Locale.getDefault())
        collator.strength = Collator.PRIMARY
        return collator.compare(this.name, other.name)
    }
}
