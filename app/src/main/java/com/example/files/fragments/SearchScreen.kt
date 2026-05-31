package com.example.files.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.files.MainActivity.instance
import com.example.files.R
import com.example.files.data.FileRepository
import com.example.files.models.JFile
import com.example.files.viewmodels.SearchViewModel
import java.util.Calendar

class SearchScreen() : Fragment() {

    var category: String = ""
    var jFiles: ArrayList<JFile>? = null

    constructor(category: String) : this() {
        this.category = category
    }

    constructor(category: String, jFiles: ArrayList<JFile>?) : this() {
        this.category = category
        this.jFiles = jFiles
    }

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = FileRepository(requireContext())
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(repository) as T
            }
        }
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]
        searchViewModel.loadFiles(category, jFiles)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        SearchScreenContent(
                            viewModel = searchViewModel,
                            onBackClick = { requireActivity().onBackPressed() },
                            showTypeFilters = category == "search" || category == "recent" || category == "downloads"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    showTypeFilters: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateLimit by remember { mutableStateOf(0L) }
    var selectedType by remember { mutableStateOf<JFile.Type?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.setTextQuery(it)
                },
                placeholder = { Text(stringResource(R.string.search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            viewModel.setTextQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
        }

        // Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val calendar = Calendar.getInstance()
            
            // Date Filters
            item {
                FilterChip(
                    selected = selectedDateLimit > 0,
                    onClick = {
                        if (selectedDateLimit > 0) {
                            selectedDateLimit = 0L
                            viewModel.clearDateFilter()
                        } else {
                            calendar.add(Calendar.HOUR_OF_DAY, -24)
                            selectedDateLimit = calendar.timeInMillis
                            viewModel.setDateFilter(selectedDateLimit)
                        }
                    },
                    label = { Text(stringResource(R.string.filter_today)) }
                )
            }
            
            // Type Filters (if applicable)
            if (showTypeFilters) {
                item {
                    FilterChip(
                        selected = selectedType == JFile.Type.IMAGE,
                        onClick = {
                            if (selectedType == JFile.Type.IMAGE) {
                                selectedType = null
                                viewModel.clearTypeFilter()
                            } else {
                                selectedType = JFile.Type.IMAGE
                                viewModel.setTypeFilter(JFile.Type.IMAGE)
                            }
                        },
                        label = { Text(stringResource(R.string.pictures)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == JFile.Type.AUDIO,
                        onClick = {
                            if (selectedType == JFile.Type.AUDIO) {
                                selectedType = null
                                viewModel.clearTypeFilter()
                            } else {
                                selectedType = JFile.Type.AUDIO
                                viewModel.setTypeFilter(JFile.Type.AUDIO)
                            }
                        },
                        label = { Text(stringResource(R.string.audio)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == JFile.Type.VIDEO,
                        onClick = {
                            if (selectedType == JFile.Type.VIDEO) {
                                selectedType = null
                                viewModel.clearTypeFilter()
                            } else {
                                selectedType = JFile.Type.VIDEO
                                viewModel.setTypeFilter(JFile.Type.VIDEO)
                            }
                        },
                        label = { Text(stringResource(R.string.video)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text(
                text = stringResource(R.string.items, uiState.searchResults.size.toString()),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(uiState.searchResults) { file ->
                    FileRowItem(file = file)
                }
            }
        }
    }
}

@Composable
fun FileRowItem(file: JFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle file click via MainActivity action */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${file.size} bytes", // Simplified for demonstration
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}