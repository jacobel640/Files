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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class FilesUiState(
    val isLoading: Boolean = false,
    val currentPath: String = "",
    val currentPathName: String = "",
    val files: List<JFile> = emptyList(),
    val selectedFiles: List<JFile> = emptyList(),
    val isGridView: Boolean = false,
    val isAllSelectedFavorites: Boolean = false,
    val mode: FilesMode = FilesMode.Normal,
    val error: String? = null
)

sealed class FilesMode {
    object Normal : FilesMode()
    object Recent : FilesMode()
    object Favorites : FilesMode()
    data class Category(val name: String) : FilesMode()
    data class Zipped(val file: java.io.File) : FilesMode()
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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, mode = mode)
            try {
                var newFiles: List<JFile> = emptyList()
                when (mode) {
                    is FilesMode.Recent -> {
                        newFiles = refreshRecents(context)
                        _uiState.value = _uiState.value.copy(currentPathName = context.getString(com.example.files.R.string.recent_files))
                    }
                    is FilesMode.Favorites -> {
                        newFiles = refreshFavorites(context)
                        _uiState.value = _uiState.value.copy(currentPathName = context.getString(com.example.files.R.string.favorites))
                    }
                    is FilesMode.Category -> {
                        newFiles = getSFiles(context, mode.name)
                        _uiState.value = _uiState.value.copy(currentPathName = mode.name)
                    }
                    is FilesMode.Zipped -> {
                        newFiles = getJFiles(context, getFilesList(mode.file))
                        _uiState.value = _uiState.value.copy(currentPathName = mode.file.name)
                    }
                    is FilesMode.Normal -> {
                        if (Statics.folder != null) {
                            newFiles = getJFiles(context, getFilesList(Statics.folder))
                            _uiState.value = _uiState.value.copy(
                                currentPath = Statics.folder.path,
                                currentPathName = Statics.folder.name
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
                if (!jFile.isDirectory && !jFile.isHidden) {
                    recent.add(jFile)
                    count++
                }
            }
        }
        return recent
    }

    private fun refreshFavorites(context: Context): List<JFile> {
        val favsDb = com.example.files.database.DBHelper(context)
        return favsDb.allPaths.map { JFile(it, context) }
    }

    private fun getSFiles(context: Context, categoryName: String): List<JFile> {
        val files = mutableListOf<JFile>()
        val uri = android.provider.MediaStore.Files.getContentUri("external")
        val projection = arrayOf(android.provider.MediaStore.Files.FileColumns.DATA)
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        
        when (categoryName) {
            context.getString(com.example.files.R.string.pictures) -> {
                selection = android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            }
            context.getString(com.example.files.R.string.video) -> {
                selection = android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            }
            context.getString(com.example.files.R.string.audio) -> {
                selection = android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
            }
            context.getString(com.example.files.R.string.documents) -> {
                selection = android.provider.MediaStore.Files.FileColumns.MIME_TYPE + " LIKE ? OR " + android.provider.MediaStore.Files.FileColumns.MIME_TYPE + " LIKE ?"
                selectionArgs = arrayOf("application/pdf", "text/%")
            }
            context.getString(com.example.files.R.string.installations) -> {
                selection = android.provider.MediaStore.Files.FileColumns.DATA + " LIKE ?"
                selectionArgs = arrayOf("%.apk")
            }
        }
        
        if (selection != null) {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol)
                    files.add(JFile(path, context))
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
