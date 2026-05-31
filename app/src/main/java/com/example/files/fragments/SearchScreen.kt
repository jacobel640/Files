package com.example.files.fragments

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.files.MainActivity.instance
import com.example.files.R
import com.example.files.Statics
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    var filtersExpanded by remember { mutableStateOf(false) }

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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                        imageVector = if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand Filters"
                                    )
                                }
                            }

                            AnimatedVisibility(visible = filtersExpanded) {
                                Column(modifier = Modifier.fillMaxWidth()) {
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
                                        val calendar = Calendar.getInstance()
                                        
                                        val dateOptions = listOf(
                                            Pair(stringResource(R.string.filter_today), -1),
                                            Pair(stringResource(R.string.filter_three_days_ago), -3),
                                            Pair(stringResource(R.string.filter_this_week), -7),
                                            Pair(stringResource(R.string.filter_this_month), -30)
                                        )

                                        items(dateOptions) { (label, daysOffset) ->
                                            FilterChip(
                                                selected = selectedDateLimit > 0 && selectedDateLimit == getTimeOffset(daysOffset),
                                                onClick = {
                                                    val limit = getTimeOffset(daysOffset)
                                                    if (selectedDateLimit == limit) {
                                                        selectedDateLimit = 0L
                                                        viewModel.clearDateFilter()
                                                    } else {
                                                        selectedDateLimit = limit
                                                        viewModel.setDateFilter(selectedDateLimit)
                                                    }
                                                },
                                                label = { Text(label) }
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
                                            contentPadding = PaddingValues(horizontal = 16.dp, bottom = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val typeOptions = listOf(
                                                Pair(stringResource(R.string.pictures), JFile.Type.IMAGE),
                                                Pair(stringResource(R.string.audio), JFile.Type.AUDIO),
                                                Pair(stringResource(R.string.video), JFile.Type.VIDEO),
                                                Pair(stringResource(R.string.documents), JFile.Type.DOCUMENT),
                                                Pair(stringResource(R.string.installations), JFile.Type.APK)
                                            )

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
                                                    },
                                                    label = { Text(label) }
                                                )
                                            }
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
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun FileRowItem(file: JFile) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (Statics.multiSelected) {
                        instance.eventListener.onItemClick(null, 0) // Mock behavior if needed, or handle directly
                    } else if (file.isDirectory) {
                        Statics.openFolder(file)
                    } else {
                        Statics.openFile(file, context)
                    }
                },
                onLongClick = {
                    if (!Statics.multiSelected) {
                        if (file.parentFile != null) {
                            Statics.openFolder(file.parentFile!!)
                            Statics.currentFragment?.select(file.path)
                        }
                    }
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (file.type == JFile.Type.IMAGE || file.type == JFile.Type.VIDEO || file.type == JFile.Type.APK) {
                GlideImage(
                    model = file.path,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = file.stringDate,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(3f)
                )
                Text(
                    text = Formatter.formatShortFileSize(context, file.size),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}