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
    val error: String? = null
)

@HiltViewModel
class FilesViewModel @Inject constructor() : ViewModel() {

    var isDragging = false
    var lastDragEndTime = 0L

    private val _uiState = MutableStateFlow(FilesUiState(
        isGridView = Statics.FOLDER_VIEW_TYPE == com.example.files.JFileAdapter.ViewType.GRID
    ))
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    fun refreshList(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Just sync with Statics for now to mimic legacy behavior
            val filesList = ArrayList<JFile>() 
            val currentPath = Statics.folder?.path ?: ""
            val currentPathName = Statics.folder?.name ?: ""
            
            Statics.folder?.listFiles()?.let { array ->
                for (file in array) {
                    if (!com.example.files.Statics.showHiddenFiles && file.name.startsWith(".")) continue
                    filesList.add(JFile(file, context))
                }
            }
            com.example.files.actions.DialogSort.sort(filesList)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentPath = currentPath,
                currentPathName = currentPathName,
                isGridView = Statics.FOLDER_VIEW_TYPE == com.example.files.JFileAdapter.ViewType.GRID,
                files = filesList
            )
        }
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
