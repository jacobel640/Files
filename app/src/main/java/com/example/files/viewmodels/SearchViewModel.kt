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
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<JFile> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: FileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allFiles: List<JFile> = emptyList()
    
    private var textQuery: String = ""
    private var dateLimit: Long = 0L
    private var typeFilter: JFile.Type? = null

    fun loadFiles(category: String, initialFiles: ArrayList<JFile>?, folderPath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (initialFiles != null && initialFiles.isNotEmpty()) {
                    allFiles = initialFiles
                } else {
                    allFiles = when (category) {
                        "search" -> repository.getAllSearchFiles()
                        "recent" -> repository.getRecentFiles()
                        "folder" -> {
                            if (folderPath != null) {
                                repository.iterateFolder(File(folderPath))
                            } else {
                                emptyList()
                            }
                        }
                        else -> emptyList()
                    }
                }
                applyFilters()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setTextQuery(query: String) {
        textQuery = query
        applyFilters()
    }

    fun setDateFilter(limit: Long) {
        dateLimit = limit
        applyFilters()
    }

    fun clearDateFilter() {
        dateLimit = 0L
        applyFilters()
    }

    fun setTypeFilter(type: JFile.Type?) {
        typeFilter = type
        applyFilters()
    }

    fun clearTypeFilter() {
        typeFilter = null
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = allFiles
        
        if (textQuery.isNotBlank()) {
            val q = textQuery.lowercase()
            filteredList = filteredList.filter { it.name.lowercase().contains(q) }
        }
        
        if (dateLimit > 0L) {
            filteredList = filteredList.filter { it.lastModified() >= dateLimit }
        }
        
        if (typeFilter != null) {
            filteredList = filteredList.filter { it.type == typeFilter }
        }
        
        _uiState.value = _uiState.value.copy(
            searchResults = filteredList
        )
    }
}
