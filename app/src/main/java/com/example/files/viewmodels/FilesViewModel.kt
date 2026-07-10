package com.example.files.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.files.Statics
import com.example.files.models.JFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class FilesUiState(
    val isLoading: Boolean = true,
    val currentPath: String = "",
    val currentPathName: String = "",
    val files: List<JFile> = emptyList(),
    val selectedFiles: List<JFile> = emptyList(),
    val isGridView: Boolean = false,
    val isAllSelectedFavorites: Boolean = false,
    val mode: FilesMode = FilesMode.Normal(),
    val error: String? = null
)

sealed class FilesMode {
    data class Normal(val file: File? = null) : FilesMode()
    object Recent : FilesMode()
    object Favorites : FilesMode()
    data class Category(val name: String) : FilesMode()
    data class Zipped(val file: File) : FilesMode()
}

@HiltViewModel
class FilesViewModel @Inject constructor() : ViewModel() {

    var isDragging = false
    var lastDragEndTime = 0L

    private val _uiState = MutableStateFlow(FilesUiState(
        isGridView = Statics.FOLDER_VIEW_TYPE == com.example.files.JFileAdapter.ViewType.GRID
    ))
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    fun refreshList(context: Context, mode: FilesMode = _uiState.value.mode) {
        val pathName = when (mode) {
            is FilesMode.Recent -> context.getString(com.example.files.R.string.recent_files)
            is FilesMode.Favorites -> context.getString(com.example.files.R.string.favorites)
            is FilesMode.Category -> {
                when (mode.name) {
                    "picture" -> context.getString(com.example.files.R.string.pictures)
                    "video" -> context.getString(com.example.files.R.string.video)
                    "audio" -> context.getString(com.example.files.R.string.audio)
                    "apk" -> context.getString(com.example.files.R.string.installations)
                    "document" -> context.getString(com.example.files.R.string.documents)
                    "archive" -> context.getString(com.example.files.R.string.compressed)
                    "downloads" -> context.getString(com.example.files.R.string.downloads)
                    else -> mode.name
                }
            }
            is FilesMode.Zipped -> mode.file.name
            is FilesMode.Normal -> mode.file?.name ?: context.getString(com.example.files.R.string.internal_storage)
        }
        val path = when (mode) {
            is FilesMode.Normal -> mode.file?.path ?: android.os.Environment.getExternalStorageDirectory().path
            is FilesMode.Zipped -> mode.file.path
            else -> pathName
        }

        _uiState.value = _uiState.value.copy(isLoading = true, mode = mode, currentPath = path, currentPathName = pathName)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var newFiles: List<JFile> = emptyList()
                when (mode) {
                    is FilesMode.Recent -> {
                        newFiles = refreshRecents(context)
                    }
                    is FilesMode.Favorites -> {
                        newFiles = refreshFavorites(context)
                    }
                    is FilesMode.Category -> {
                        newFiles = getSFiles(context, mode.name)
                    }
                    is FilesMode.Zipped -> {
                        newFiles = getJFiles(context, getFilesList(mode.file))
                        _uiState.value = _uiState.value.copy(currentPath = mode.file.path, currentPathName = mode.file.name)
                    }
                    is FilesMode.Normal -> {
                        val folder = mode.file ?: Statics.folder
                        if (folder != null) {
                            newFiles = getJFiles(context, getFilesList(folder))
                            _uiState.value = _uiState.value.copy(
                                currentPath = folder.path,
                                currentPathName = folder.name
                            )
                        }
                    }
                }
                
                val filesList = ArrayList(newFiles)
                com.example.files.actions.DialogSort.sort(filesList)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isGridView = Statics.FOLDER_VIEW_TYPE == com.example.files.JFileAdapter.ViewType.GRID,
                    files = filesList
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun refreshRecents(context: Context): List<JFile> {
        val recent = mutableListOf<JFile>()
        val sortOrder = android.provider.MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        val uri = android.provider.MediaStore.Files.getContentUri("external")
        val projection = arrayOf(android.provider.MediaStore.Files.FileColumns.DATA)
        context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val dataCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DATA)
            var count = 0
            while (cursor.moveToNext() && count < 2000) {
                val path = cursor.getString(dataCol)
                val jFile = JFile(path, context)
                if (filterRecent(jFile)) {
                    val m60DaysAgo = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -60) }.timeInMillis
                    if (jFile.lastModified() > m60DaysAgo) {
                        recent.add(jFile)
                        count++
                    } else break
                }
            }
        }
        return recent
    }

    private fun refreshFavorites(context: Context): List<JFile> {
        val favsDb = com.example.files.database.DBHelper(context)
        return favsDb.allPaths.map { JFile(it, context) }
    }

    private fun filterRecent(file: File): Boolean {
        return file.isFile &&
                file.name.contains(".") &&
                !file.isDirectory && file.length() != 0L &&
                !file.absolutePath.startsWith("/storage/emulated/0/Android") &&
                !(file.parentFile?.path?.endsWith("WhatsApp/Databases") ?: false) &&
                !(file.parentFile?.path?.endsWith("WhatsApp/Backups") ?: false) &&
                isRecentValid(file.name.substring(file.name.lastIndexOf(".") + 1).lowercase())
    }

    private fun isRecentValid(extension: String): Boolean {
        return when (extension) {
            "", "m3u", "log", "tmp", "temp", "bak", "bkup", "backup", "crypt1", "crypt12" -> false
            else -> true
        }
    }

    private fun getSFiles(context: Context, categoryName: String): List<JFile> {
        val files = mutableListOf<JFile>()
        var uri = android.provider.MediaStore.Files.getContentUri("external")
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        var sortOrder: String? = android.provider.MediaStore.Audio.Media.DATE_MODIFIED + " ASC"

        when (categoryName) {
            "picture" -> {
                uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL)
                } else android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            "video" -> {
                uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL)
                } else android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            "audio" -> {
                uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    android.provider.MediaStore.Audio.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL)
                } else android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            "downloads" -> {
                selection = android.provider.MediaStore.Files.FileColumns.DATA + " LIKE ?"
                selectionArgs = arrayOf("%/Download/%")
            }
            "apk" -> {
                selection = android.provider.MediaStore.Files.FileColumns.DATA + " LIKE ?"
                selectionArgs = arrayOf("%.apk")
            }
            "archive" -> {
                // ZIPs will be handled via extension filtering later if needed, but for now we fetch all or filter by mime
            }
            else -> return emptyList()
        }

        context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.Files.FileColumns.DATA), selection, selectionArgs, sortOrder)?.use { cursor ->
            val dataCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol)
                val jFile = JFile(path, context)
                if (jFile.length() != 0L) {
                    if (categoryName == "archive") {
                        if (jFile.type == JFile.Type.ARCHIVE) files.add(jFile)
                    } else if (categoryName == "apk" || categoryName == "downloads" || categoryName == "picture" || categoryName == "audio" || categoryName == "video") {
                        if (jFile.type != JFile.Type.FOLDER) files.add(jFile)
                    } else {
                        if (jFile.nameTLC.endsWith("." + categoryName.lowercase()) && jFile.isFile) {
                            files.add(jFile)
                        }
                    }
                }
            }
        }
        return files
    }

    private fun getFilesList(folder: java.io.File): List<java.io.File> {
        return folder.listFiles()?.toList() ?: emptyList()
    }

    private fun getJFiles(context: Context, files: List<java.io.File>): List<JFile> {
        return files.filter { com.example.files.Statics.showHiddenFiles || !it.name.startsWith(".") }.map { JFile(it, context) }
    }

    fun toggleViewType() {
        val newIsGrid = !_uiState.value.isGridView
        _uiState.value = _uiState.value.copy(isGridView = newIsGrid)
        Statics.FOLDER_VIEW_TYPE = if (newIsGrid) com.example.files.JFileAdapter.ViewType.GRID else com.example.files.JFileAdapter.ViewType.ROW
    }

    fun toggleSelection(file: JFile) {
        val current = _uiState.value.selectedFiles.toMutableList()
        if (current.contains(file)) {
            current.remove(file)
        } else {
            current.add(file)
        }
        updateSelection(current)
    }

    fun selectFile(file: JFile) {
        val current = _uiState.value.selectedFiles.toMutableList()
        if (!current.contains(file)) {
            current.add(file)
            updateSelection(current)
        }
    }

    private var preDragSelection = emptyList<JFile>()

    fun startDragSelect(initialIndex: Int) {
        preDragSelection = _uiState.value.selectedFiles.toList()
    }

    fun selectRange(start: Int, end: Int) {
        val current = preDragSelection.toMutableList()
        val files = _uiState.value.files
        if (files.isEmpty()) return
        val safeStart = maxOf(0, minOf(start, files.size - 1))
        val safeEnd = maxOf(0, minOf(end, files.size - 1))
        val min = minOf(safeStart, safeEnd)
        val max = maxOf(safeStart, safeEnd)
        for (i in min..max) {
            val file = files[i]
            if (preDragSelection.contains(file)) {
                current.remove(file)
            } else {
                current.add(file)
            }
        }
        updateSelection(current)
    }

    fun selectAll() {
        val currentFiles = _uiState.value.files
        if (_uiState.value.selectedFiles.size == currentFiles.size && currentFiles.isNotEmpty()) {
            clearSelection()
        } else {
            updateSelection(currentFiles)
        }
    }

    fun clearSelection() {
        updateSelection(emptyList())
    }

    private fun updateSelection(selected: List<JFile>) {
        val allFavs = if (selected.isEmpty()) false else selected.all { file ->
            Statics.favorites.allPaths.any { fav -> fav.path == file.path }
        }
        _uiState.value = _uiState.value.copy(
            selectedFiles = selected,
            isAllSelectedFavorites = allFavs
        )
        // sync with statics
        Statics.selectedJFiles.clear()
        Statics.selectedJFiles.addAll(selected)
        Statics.multiSelected = selected.isNotEmpty()
    }

    fun toggleFavorite(context: Context) {
        val selected = _uiState.value.selectedFiles
        if (_uiState.value.isAllSelectedFavorites) {
            selected.forEach { file ->
                val fav = Statics.favorites.allPaths.find { it.path == file.path }
                if (fav != null) Statics.favorites.deletePath(fav.id)
            }
        } else {
            selected.forEach { file ->
                Statics.favorites.addToFavorites(file)
            }
        }
        updateSelection(selected)
    }

    fun resortIfSize() {
        if (com.example.files.Statics.sort == 1) {
            val filesList = ArrayList(_uiState.value.files)
            com.example.files.actions.DialogSort.sort(filesList)
            _uiState.value = _uiState.value.copy(files = filesList)
        }
    }
}
