package com.example.files.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.example.files.R
import com.example.files.Statics
import com.example.files.models.JFile
import com.example.files.ui.theme.FilesTheme
import com.example.files.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
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
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        searchViewModel.loadFiles(category, jFiles, Statics.folder?.path)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FilesTheme {
                    Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                        SearchScreenContent(
                            viewModel = searchViewModel,
                            onBackClick = { requireActivity().onBackPressed() },
                            showTypeFilters = true,
                            category = category,
                            initialFiles = jFiles
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    showTypeFilters: Boolean = true,
    category: String,
    initialFiles: ArrayList<JFile>? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }

    var selectedDateLimit by remember { mutableStateOf<Int?>(if (category == "recent") -60 else null) }
    var selectedType by remember {
        mutableStateOf<JFile.Type?>(
            when (category) {
                "picture" -> JFile.Type.IMAGE
                "video" -> JFile.Type.VIDEO
                "audio" -> JFile.Type.AUDIO
                "document" -> JFile.Type.DOCUMENT
                "apk" -> JFile.Type.APK
                "archive" -> JFile.Type.ARCHIVE
                else -> null
            }
        )
    }
    var searchInFolder by remember { mutableStateOf(category == "folder" || category == "downloads") }
    val context = LocalContext.current
    var hasReloadedAllFiles by remember { mutableStateOf(category == "search" || category == "folder" || category == "downloads") }
    val hasActiveFilters = searchInFolder || selectedDateLimit != null || selectedType != null

    fun checkReload() {
        if (!hasReloadedAllFiles) {
            hasReloadedAllFiles = true
            viewModel.reloadAllFiles()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = stringResource(R.string.search),
                    fontSize = 35.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    textAlign = TextAlign.Center
                )
            }

            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Search Bar Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = "Back")
                            }
                            TextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    viewModel.setTextQuery(it)
                                },
                                placeholder = { Text(stringResource(R.string.search)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
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
                        if (uiState.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // Collapsible Filters Card
                    val cardContainerColor = if (filtersExpanded) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (!filtersExpanded && hasActiveFilters)
                                            MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        shape = RoundedCornerShape(0.dp)
                                    )
                                    .clickable { filtersExpanded = !filtersExpanded }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (filtersExpanded) stringResource(R.string.close) else stringResource(R.string.filters),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.items, uiState.searchResults.size.toString()),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Icon(
                                        imageVector = if (filtersExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand Filters"
                                    )
                                }
                            }

                            val dateOptions = listOf(
                                Pair(stringResource(R.string.filter_two_months), -60),
                                Pair(stringResource(R.string.filter_today), -1),
                                Pair(stringResource(R.string.filter_three_days_ago), -3),
                                Pair(stringResource(R.string.filter_this_week), -7),
                                Pair(stringResource(R.string.filter_this_month), -30)
                            )
                            val typeOptions = listOf(
                                Pair(stringResource(R.string.pictures), JFile.Type.IMAGE),
                                Pair(stringResource(R.string.audio), JFile.Type.AUDIO),
                                Pair(stringResource(R.string.video), JFile.Type.VIDEO),
                                Pair(stringResource(R.string.documents), JFile.Type.DOCUMENT),
                                Pair(stringResource(R.string.installations), JFile.Type.APK),
                                Pair(stringResource(R.string.compressed), JFile.Type.ARCHIVE)
                            )

                            AnimatedVisibility(visible = filtersExpanded) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if ((category == "folder" && Statics.folder != null) || category == "downloads") {
                                        Text(
                                            text = stringResource(R.string.location),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                        )
                                        FilterChip(
                                            selected = searchInFolder,
                                            onClick = { 
                                                searchInFolder = !searchInFolder
                                                if (searchInFolder) {
                                                    viewModel.loadFiles(
                                                        category,
                                                        initialFiles,
                                                        if (category == "downloads") null else Statics.folder?.path
                                                    )
                                                } else {
                                                    viewModel.loadFiles("search", null, null)
                                                }
                                            },
                                            label = {
                                                if (category == "downloads") {
                                                    Text(
                                                        stringResource(
                                                            R.string.search_in_folder,
                                                            stringResource(R.string.downloads)
                                                        )
                                                    )
                                                } else {
                                                    val folderName =
                                                        com.example.files.utils.PathFormatter(
                                                            context
                                                        ).format(Statics.folder!!.path).split("/")
                                                            .last()
                                                    Text(
                                                        stringResource(
                                                            R.string.search_in_folder,
                                                            folderName
                                                        )
                                                    )
                                                }
                                            },
                                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                                        )
                                    }

                                    Text(
                                        text = stringResource(R.string.time_title),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                    )

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(dateOptions) { (label, daysOffset) ->
                                            FilterChip(
                                                selected = selectedDateLimit == daysOffset,
                                                onClick = {
                                                    if (selectedDateLimit == daysOffset) {
                                                        selectedDateLimit = null
                                                        viewModel.clearDateFilter()
                                                    } else {
                                                        selectedDateLimit = daysOffset
                                                        viewModel.setDateFilter(getTimeOffset(daysOffset))
                                                    }
                                                    if (category == "recent") {
                                                        checkReload()
                                                    }
                                                },
                                                label = { Text(label) },
                                                leadingIcon = {
                                                    if (uiState.isFiltering && selectedDateLimit == daysOffset) {
                                                        androidx.compose.material3.CircularProgressIndicator(
                                                            modifier = Modifier
                                                                .padding(end = 4.dp)
                                                                .size(16.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    if (showTypeFilters) {
                                        Text(
                                            text = stringResource(R.string.sort_type),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                        )
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(typeOptions) { (label, type) ->
                                                FilterChip(
                                                    selected = selectedType == type,
                                                    onClick = {
                                                        if (selectedType == type) {
                                                            selectedType = null
                                                            viewModel.setTypeFilter(null)
                                                        } else {
                                                            selectedType = type
                                                            viewModel.setTypeFilter(type)
                                                        }
                                                        if (category != "recent" && category != "folder" && category != "downloads" && category != "search") {
                                                            checkReload()
                                                        }
                                                    },
                                                    label = { Text(label) },
                                                    leadingIcon = {
                                                        if (uiState.isFiltering && selectedType == type) {
                                                            androidx.compose.material3.CircularProgressIndicator(
                                                                modifier = Modifier
                                                                    .padding(end = 4.dp)
                                                                    .size(16.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = !filtersExpanded && hasActiveFilters) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp, bottom = 10.dp),
                                    contentPadding = PaddingValues(start = 6.dp, end = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    stickyHeader {
                                        Surface(color = cardContainerColor) {
                                            FilledIconButton(
                                                onClick = {
                                                    if (searchInFolder) {
                                                        searchInFolder = false
                                                        viewModel.loadFiles("search", null, null)
                                                    }
                                                    if (selectedDateLimit != null) {
                                                        selectedDateLimit = null
                                                        viewModel.clearDateFilter()
                                                    }
                                                    if (selectedType != null) {
                                                        selectedType = null
                                                        viewModel.setTypeFilter(null)
                                                    }
                                                    if (category != "search") checkReload()
                                                },
                                                shape = CircleShape
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = "Clear All",
                                                    tint = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            }
                                        }
                                    }

                                    if (searchInFolder) {
                                        item {
                                            FilterChip(
                                                selected = true,
                                                onClick = {
                                                    searchInFolder = false
                                                    viewModel.loadFiles("search", null, null)
                                                },
                                                label = {
                                                    if (category == "downloads") {
                                                        Text(
                                                            stringResource(
                                                                R.string.search_in_folder,
                                                                stringResource(R.string.downloads)
                                                            )
                                                        )
                                                    } else {
                                                        val folderName =
                                                            com.example.files.utils.PathFormatter(
                                                                context
                                                            ).format(Statics.folder!!.path)
                                                                .split("/").last()
                                                        Text(
                                                            stringResource(
                                                                R.string.search_in_folder,
                                                                folderName
                                                            )
                                                        )
                                                    }
                                                },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    if (selectedDateLimit != null) {
                                        item {
                                            val label =
                                                dateOptions.find { it.second == selectedDateLimit }?.first
                                                    ?: ""
                                            FilterChip(
                                                selected = true,
                                                onClick = {
                                                    selectedDateLimit = null
                                                    viewModel.clearDateFilter()
                                                    if (category == "recent") checkReload()
                                                },
                                                label = { Text(label) },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    if (selectedType != null) {
                                        item {
                                            val label =
                                                typeOptions.find { it.second == selectedType }?.first
                                                    ?: ""
                                            FilterChip(
                                                selected = true,
                                                onClick = {
                                                    selectedType = null
                                                    viewModel.setTypeFilter(null)
                                                    if (category != "recent" && category != "folder" && category != "downloads" && category != "search") checkReload()
                                                },
                                                label = { Text(label) },
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!uiState.isLoading && uiState.searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_files_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(uiState.searchResults, key = { it.path }) { file ->
                    FileRowItem(file = file)
                }
            }
        }
    }
}

fun getTimeOffset(days: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, days)
    return cal.timeInMillis
}
