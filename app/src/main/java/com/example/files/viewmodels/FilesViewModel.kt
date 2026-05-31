package com.example.files.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.files.data.FileRepository
import com.example.files.models.JFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FilesUiState(
    val isLoading: Boolean = false,
    val currentPath: String = "",
    val files: List<JFile> = emptyList(),
    val error: String? = null
)

class FilesViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    fun loadPath(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPath = path)
            // Example:
            // val files = repository.getFilesInPath(path)
            _uiState.value = _uiState.value.copy(isLoading = false, files = emptyList())
        }
    }
}
