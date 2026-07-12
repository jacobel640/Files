package com.example.files

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW_PERMISSION_USAGE
import android.content.Intent.EXTRA_ALLOW_MULTIPLE
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.files.MainActivity.editor
import com.example.files.MainActivity.instance
import com.example.files.actions.DialogBase
import com.example.files.actions.DialogDelete
import com.example.files.database.DBHelper
import com.example.files.models.JFile
import com.example.files.models.JFile.Type.ARCHIVE
import com.example.files.models.JFile.Type.SHORTCUT
import com.example.files.presentation.files_explorer.FilesFragment
import com.example.files.presentation.search.SearchScreen
import com.example.files.utils.MainActivityUtils.Storages.storageItems
import com.example.files.view.Note
import com.google.android.material.snackbar.Snackbar
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.PopupStyles
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.io.File
import java.io.IOException

object Statics {

    @JvmField @SuppressLint("StaticFieldLeak") var actionBar: View? = null
    @JvmField @SuppressLint("StaticFieldLeak") var shortAnimationDuration: Int = 0
    @JvmField @SuppressLint("StaticFieldLeak") var move: LinearLayout? = null
    @JvmField @SuppressLint("StaticFieldLeak") var copy: LinearLayout? = null
    @JvmField @SuppressLint("StaticFieldLeak") var details: LinearLayout? = null
    @JvmField @SuppressLint("StaticFieldLeak") var share: LinearLayout? = null
    @JvmField @SuppressLint("StaticFieldLeak") var delete: LinearLayout? = null
    @JvmField var mainLayout: CoordinatorLayout? = null
    @JvmField var selectedJFiles: ArrayList<JFile> = ArrayList()
    @JvmField var multiSelected = false
    @JvmField var copyMode = false
    @JvmField var showHiddenFiles = false
    @JvmField var showFileSize = false
    @JvmField var isSingleLine = false
    @JvmField var showRecent = false
    @JvmField var showCategories = false
    @JvmField var showFavorites = false
    @JvmField var sort = 0
    @JvmField var order = 0
    @JvmField var folder: File? = null
    @JvmField var tempFolder: File? = null
    @JvmField var highlightFile: String? = null

    const val DOC_SLASH = "%2F"
    const val DOC_SPACE = "%20"
    const val OPS_CHANNEL_ID = "operations_channel"
    const val TAG_FOLDER = "folder"
    const val TAG_CATEGORY = "category"
    const val TAG_SEARCH = "search"
    const val TAG_RECENT = "recent"
    const val TAG_ZIPPED = "zipped"

    @JvmField var FOLDER_VIEW_TYPE = JFileAdapter.ViewType.ROW
    @JvmField var CATEGORY_VIEW_TYPE = JFileAdapter.ViewType.ROW

    const val BYTE = 10
    const val KB = 10240
    const val MB = 20480
    const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 4010

    @JvmField @SuppressLint("StaticFieldLeak") var currentFragment: FilesFragment? = null
    @JvmField @SuppressLint("StaticFieldLeak") var searchFragment: SearchScreen? = null

    enum class FragmentType { FILES, CATEGORY, MAIN, RECENT, FAVORITES, ARCHIVE, SEARCH }

    @JvmField @SuppressLint("StaticFieldLeak") var actions: ArrayList<DialogBase> = ArrayList()
    @JvmField var favorites: DBHelper? = null

    @JvmStatic fun prepareAction(action: DialogBase) {
        actions.add(action)
        action.preAction()
    }

    @JvmStatic fun getPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

    @JvmStatic fun startCurrentAction() {
        if (actions.isNotEmpty()) actions[actions.size - 1].startAction(folder)
    }

    @JvmStatic fun removeCurrentAction() {
        if (actions.isNotEmpty()) actions.removeAt(actions.size - 1)
    }

    @JvmStatic fun openFile(jFile: JFile, context: Context) {
        if (jFile.type == ARCHIVE) {
            openZipFile(jFile)
            return
        } else if (jFile.type == SHORTCUT) {
            openShortcut(jFile)
            return
        }
        val uri: Uri = if (jFile.isDocumentFile()) jFile.getDocumentFile()!!.uri else FileProvider.getUriForFile(context, getPackageName(), jFile)
        val mimeType = MimeTypeMap.getSingleton()
        val extension = jFile.name.substringAfterLast('.', "").lowercase()
        var type = mimeType.getMimeTypeFromExtension(extension)
        if (type == null) type = "*/*"
        val view = Intent(Intent.ACTION_VIEW)
        view.setDataAndType(uri, type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.putExtra(ACTION_VIEW_PERMISSION_USAGE, true)
        }
        view.putExtra(EXTRA_ALLOW_MULTIPLE, true)
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(view)
        } catch (e: ActivityNotFoundException) {
            Note(instance, e.message).show()
        }
    }

    @JvmStatic private fun openShortcut(jFile: JFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Mimicking original behavior
                val lines = File(jFile.path).readLines()
                val file = File(lines.toString())
                if (file.exists()) {
                    openFolder(file)
                } else {
                    mainLayout?.let {
                        Snackbar.make(it, "Source not found, Delete Shortcut?", Snackbar.LENGTH_SHORT)
                            .setAction(R.string.delete) { _ ->
                                val temp = ArrayList<JFile>()
                                temp.add(jFile)
                                prepareAction(DialogDelete(temp))
                            }.show()
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    @JvmStatic fun openFileWith(jFile: JFile, context: Context) {
        instance!!.eventListener.onMultiSelectedChange(false)
        val uri: Uri = if (jFile.isDocumentFile()) jFile.getDocumentFile()!!.uri else FileProvider.getUriForFile(context, getPackageName(), jFile)
        val view = Intent(Intent.ACTION_VIEW)
        view.setDataAndType(uri, "*/*")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.putExtra(ACTION_VIEW_PERMISSION_USAGE, true)
        }
        view.putExtra(EXTRA_ALLOW_MULTIPLE, true)
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(view)
        } catch (e: ActivityNotFoundException) {
            Note(instance, e.message).show()
        }
    }

    @JvmStatic fun openZipFile(jFile: JFile) {
        if (jFile.type != ARCHIVE) return
        if (multiSelected) instance!!.eventListener.onMultiSelectedChange(false)
        instance!!.loadFragment(instance!!.newZippedFragment(jFile), TAG_ZIPPED)
    }

    @JvmStatic fun openFolder(file: File, highlight: String?) {
        highlightFile = highlight
        openFolder(file)
    }

    @JvmStatic fun openFolder(file: File) {
        tempFolder = file
        if (multiSelected) instance!!.eventListener.onMultiSelectedChange(false)
        if (!instance!!.permissionGranted()) {
            instance!!.requestStoragePermissions()
            return
        }
        val isVisible = isVisible(TAG_FOLDER)
        if (!isVisible || folder?.path != file.path) {
            if (!canRead(file) && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                takeCardUriPermission(file)
            } else if (!isAndroidR(file)) {
                instance!!.loadFragment(instance!!.newFragment(file), TAG_FOLDER)
            }
        }
    }

    @JvmStatic fun OpenSearch(category: String) {
        instance!!.loadFragment(instance!!.newSFragment(category, null), TAG_SEARCH)
    }

    @JvmStatic fun OpenSearch(category: String, jFiles: ArrayList<JFile>?) {
        instance!!.loadFragment(instance!!.newSFragment(category, jFiles), TAG_SEARCH)
    }

    @JvmStatic fun OpenCategory(category: String) {
        sort = 2
        order = 1
        editor.putInt("SORT", 2).apply()
        editor.putInt("ORDER", 1).apply()
        instance!!.loadFragment(instance!!.newDFragment(category), TAG_CATEGORY)
    }

    @JvmStatic fun openRecent() {
        sort = 2
        order = 1
        editor.putInt("SORT", 2).apply()
        editor.putInt("ORDER", 1).apply()
        instance!!.loadFragment(instance!!.newRFragment(), TAG_RECENT)
    }

    @JvmStatic fun takeCardUriPermission(sdCardRoot: File) {
        val sm = instance!!.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
            var startDir = sdCardRoot.path.replace("/storage/emulated/0/", "")
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
            if (uri != null) {
                var scheme = uri.toString()
                Log.d(TAG, "INITIAL_URI scheme: $scheme")
                scheme = scheme.replace("/root/", "/document/")
                startDir = startDir.replace("/", "%2F")
                scheme += "%3A$startDir"
                uri = Uri.parse(scheme)
                intent.putExtra("android.provider.extra.INITIAL_URI", uri)
                Log.d("##### uriPermissions #####", "uri: $uri")
                instance!!.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE)
            }
            return
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        try {
            instance!!.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE)
        } catch (e: ActivityNotFoundException) {
            Log.d(TAG, "takeCardUriPermission: ${e.message}")
        }
    }

    @JvmStatic fun getUriPermission(): Uri? {
        val persistedUriPermissions = instance!!.contentResolver.persistedUriPermissions
        if (persistedUriPermissions.isNotEmpty()) {
            return persistedUriPermissions[0].uri
        }
        return null
    }

    @JvmStatic @SuppressLint("SdCardPath") fun isDocumentFile(file: File): Boolean {
        if (!file.path.startsWith("/storage/emulated")) return !file.path.startsWith("/sdcard")
        return false
    }

    @JvmStatic fun canRead(file: File): Boolean {
        return if (isExtSDCardRootDir(file)) {
            JFile(file, instance!!).getDocumentTreeSec()!!.canRead()
        } else {
            file.canRead()
        }
    }

    @JvmStatic @SuppressLint("SdCardPath") fun isRootFile(file: File): Boolean {
        var currentFile: File? = file
        while (currentFile?.parentFile != null) {
            if (currentFile.path.startsWith("/storage/emulated") ||
                currentFile.path.startsWith("/sdcard") ||
                isExternalSDCardDir(currentFile)
            ) return false
            currentFile = currentFile.parentFile
        }
        return currentFile?.path == "/"
    }

    @JvmStatic @SuppressLint("SdCardPath") private fun isExternalSDCardDir(file: File): Boolean {
        if (file.path.startsWith("/storage/emulated") || file.path.startsWith("/sdcard")) return false
        for (si in storageItems) {
            if (file.path.contains(si.file.path)) return true
        }
        return false
    }

    @JvmStatic @SuppressLint("SdCardPath") fun isExtSDCardRootDir(file: File): Boolean {
        if (file.path.startsWith("/storage/emulated") || file.path.startsWith("/sdcard")) return false
        for (si in storageItems) {
            if (file.path.endsWith(si.file.name)) return true
        }
        return false
    }

    @JvmStatic @SuppressLint("SdCardPath") fun isAndroidR(file: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (file.path == "/storage/emulated/0/Android/data" || file.path == "/sdcard/Android/data") {
                if (!JFile(file, instance!!).getDocumentTreeSec()!!.canRead()) {
                    tempFolder = file
                    takeCardUriPermission(file)
                } else return false
                return true
            }
        }
        return false
    }

    @JvmStatic @SuppressLint("UseCompatLoadingForDrawables") fun setFastScrollBar(recyclerView: RecyclerView, provider: PopupTextProvider) {
        val context = recyclerView.context
        val fastScroller = FastScrollerBuilder(recyclerView)
        fastScroller.setPopupStyle { textView ->
            PopupStyles.MD2.accept(textView)
            textView.backgroundTintMode = PorterDuff.Mode.LIGHTEN
            textView.setTextColor(context.getColor(R.color.app_theme))
        }
            .setPopupTextProvider(provider)
            .setThumbDrawable(context.getDrawable(R.drawable.scroll_bar_thumb)!!)
            .setTrackDrawable(context.getDrawable(R.drawable.scroll_bar_track)!!)
            .build()
    }

    @JvmStatic fun dpToPixels(dip: Float): Int {
        val r = instance!!.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        ).toInt()
    }

    @JvmStatic @SuppressLint("DiscouragedApi") fun hasNavigationBar(): Boolean {
        val resources = instance!!.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    @JvmStatic fun isVisible(tag: String): Boolean {
        val stackCount = instance!!.supportFragmentManager.backStackEntryCount
        if (stackCount <= 0) return false
        return instance!!.supportFragmentManager.fragments[stackCount - 1].tag == tag
    }
}
