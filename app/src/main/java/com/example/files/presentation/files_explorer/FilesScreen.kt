package com.example.files.presentation.files_explorer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.composed
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.files.MainActivity
import com.example.files.R
import com.example.files.Statics
import com.example.files.actions.DialogCopy
import com.example.files.actions.DialogCreateNew
import com.example.files.actions.DialogDelete
import com.example.files.actions.DialogDetails
import com.example.files.actions.DialogMove
import com.example.files.actions.DialogRename
import com.example.files.actions.SortDialogContent
import com.example.files.activities.SettingsActivity
import com.example.files.components.FastScroller
import com.example.files.components.FileIcon
import com.example.files.components.PathBreadcrumbs
import com.example.files.fragments.FragmentBase
import com.example.files.listeners.OnMultiSelectedChange
import com.example.files.models.JFile
import com.example.files.utils.PathFormatter
import com.example.files.viewmodels.FilesViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilesFragment : FragmentBase(FragmentType.FILES) {
    private lateinit var filesViewModel: FilesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parent = Statics.folder
        filesViewModel = ViewModelProvider(this)[FilesViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        FilesScreen(
                            viewModel = filesViewModel,
                            onNavigateBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                            onNavigateToFolder = { file -> Statics.openFolder(File(file.path)) },
                            onOpenFile = { file -> Statics.openFile(file, requireContext()) }
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(v: View?) {}

    override fun loadList() {
        filesViewModel.refreshList(requireContext())
    }

    override fun refresh() {
        filesViewModel.refreshList(requireContext())
    }

    override fun notVisible(): Boolean {
        return !Statics.isVisible(Statics.TAG_FOLDER)
    }

    // Override to prevent NPE on binding since we completely replaced the XML view
    override fun animate() {}
    override fun refreshRecyclerPadding(addSpace: Boolean) {}
    override fun refreshGrid() {}
    override fun refreshActionsList() {}
    override fun setListeners() {}

    override fun setListListener() {
        MainActivity.instance?.addMultiSelectedChangeListener(object : OnMultiSelectedChange {
            override fun onMultiSelectedChange(multiSelected: Boolean) {
                if (!multiSelected) {
                    filesViewModel.clearSelection()
                }
            }
            override fun onRefresh() {}
            override fun onRefreshActionsList() {}
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToFolder: (JFile) -> Unit,
    onOpenFile: (JFile) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalActivity.current as MainActivity
    var menuExpanded by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val formattedPath = remember(uiState.currentPath) {
        val full = PathFormatter(context).format(uiState.currentPath)
        full.split("/").lastOrNull() ?: uiState.currentPathName
    }

    // Trigger load when screen starts or path changes
    LaunchedEffect(Unit) {
        viewModel.refreshList(context)
    }

    BackHandler(enabled = uiState.selectedFiles.isNotEmpty()) {
        viewModel.clearSelection()
    }

    LaunchedEffect(uiState.selectedFiles.size) {
        if (uiState.selectedFiles.isNotEmpty()) {
            MainActivity.actionBarVisibility(View.VISIBLE)
        } else if (!Statics.copyMode) {
            MainActivity.actionBarVisibility(View.GONE)
        }
    }

    var currentSort by remember { mutableIntStateOf(Statics.sort) }
    
    if (showSortDialog) {
        ModalBottomSheet(onDismissRequest = { showSortDialog = false }) {
            SortDialogContent(
                initialSort = Statics.sort,
                initialOrder = Statics.order,
                onApply = { selectedSort, selectedOrder ->
                    Statics.sort = selectedSort
                    Statics.order = selectedOrder
                    currentSort = selectedSort
                    MainActivity.editor.putInt("SORT", selectedSort).apply()
                    MainActivity.editor.putInt("ORDER", selectedOrder).apply()
                    showSortDialog = false
                    viewModel.refreshList(context)
                },
                onCancel = { showSortDialog = false }
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                        )
                    )
                )
            ) {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    title = {
                        Row(
                            modifier = Modifier.clickable(
                                onClick = {
                                    if (uiState.selectedFiles.isNotEmpty()) viewModel.selectAll()
                                }
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if (uiState.selectedFiles.isNotEmpty()) {
                                IconButton(onClick = { viewModel.selectAll() }) {
                                    Icon(
                                        imageVector = if (uiState.selectedFiles.size == uiState.files.size && uiState.files.isNotEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = "Select All",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.selectedFiles.isNotEmpty()) {
                                    stringResource(
                                        R.string.items_chosen,
                                        uiState.selectedFiles.size.toString()
                                    )
                                } else if (uiState.currentPath == Environment.getExternalStorageDirectory().path) {
                                    stringResource(R.string.internal_storage)
                                } else {
                                    File(uiState.currentPath).name
                                },
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.selectedFiles.isNotEmpty()) {
                                viewModel.clearSelection()
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (uiState.selectedFiles.isNotEmpty()) {
                            IconButton(onClick = { viewModel.toggleFavorite(context) }) {
                                Icon(
                                    painter = painterResource(id = if (uiState.isAllSelectedFavorites) R.drawable.star else R.drawable.star_outline),
                                    contentDescription = "Favorite",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleViewType() }) {
                                Icon(
                                    imageVector = if (uiState.isGridView) Icons.AutoMirrored.Rounded.List else Icons.Rounded.GridView,
                                    contentDescription = "Toggle View",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { Statics.OpenSearch(Statics.TAG_FOLDER) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                val selectedSize = uiState.selectedFiles.size
                                if (selectedSize == 0) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.create_new)) }, onClick = { 
                                        menuExpanded = false
                                        DialogCreateNew(context, false) 
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by)) }, onClick = { 
                                        menuExpanded = false
                                        showSortDialog = true
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.settings)) }, onClick = { 
                                        menuExpanded = false
                                        context.startActivity(Intent(context, SettingsActivity::class.java))
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.refresh)) }, onClick = { 
                                        menuExpanded = false
                                        viewModel.refreshList(context)
                                    })
                                } else if (selectedSize == 1) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, onClick = { 
                                        menuExpanded = false
                                        DialogRename(context)
                                    })
                                    if (!uiState.selectedFiles[0].isDirectory) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.open_with)) }, onClick = { 
                                            menuExpanded = false
                                            Statics.openFileWith(uiState.selectedFiles[0], context)
                                        })
                                    }
                                    DropdownMenuItem(text = { Text(stringResource(R.string.details)) }, onClick = { 
                                        menuExpanded = false
                                        DialogDetails(context, false)
                                    })
                                }
                                
                                if (selectedSize > 0) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.move)) }, onClick = { 
                                        menuExpanded = false
                                        Statics.prepareAction(DialogMove(ArrayList(uiState.selectedFiles)))
                                        viewModel.clearSelection()
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.copy_action)) }, onClick = { 
                                        menuExpanded = false
                                        Statics.prepareAction(DialogCopy(ArrayList(uiState.selectedFiles)))
                                        viewModel.clearSelection()
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = { 
                                        menuExpanded = false
                                        Statics.prepareAction(DialogDelete(ArrayList(uiState.selectedFiles)))
                                        viewModel.clearSelection()
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.add_to_favorites)) }, onClick = { 
                                        menuExpanded = false
                                        uiState.selectedFiles.forEach { Statics.favorites.addToFavorites(it) }
                                        viewModel.clearSelection()
                                    })
                                }
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.selectedFiles.isEmpty(),
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    PathBreadcrumbs(
                        currentPath = uiState.currentPath,
                        onNavigate = { path ->
                            onNavigateToFolder(JFile(File(path), context as Activity))
                        },
                        onHomeClick = {
                            (context as? Activity)?.finish()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading && uiState.files.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.files.isEmpty()) {
                Text(
                    text = stringResource(R.string.nothing_in_here),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val gridState = rememberLazyGridState()
                Box(modifier = Modifier.fillMaxSize()) {
                    val density = LocalDensity.current
                    val topPaddingPx = with(density) { (paddingValues.calculateTopPadding() + if (uiState.isGridView) 8.dp else 0.dp).toPx() }
                    val startPaddingPx = with(density) { (if (uiState.isGridView) 8.dp else 0.dp).toPx() }

                    LazyVerticalGrid(
                    state = gridState,
                    columns = if (uiState.isGridView) GridCells.Fixed(4) else GridCells.Fixed(1),
                    contentPadding = PaddingValues(
                        start = if (uiState.isGridView) 8.dp else 0.dp,
                        end = if (uiState.isGridView) 8.dp else 0.dp,
                        top = paddingValues.calculateTopPadding() + if (uiState.isGridView) 8.dp else 0.dp,
                        bottom = paddingValues.calculateBottomPadding() + 80.dp + if (uiState.isGridView) 8.dp else 0.dp
                    ),
                    horizontalArrangement = if (uiState.isGridView) Arrangement.spacedBy(4.dp) else Arrangement.Start,
                    verticalArrangement = if (uiState.isGridView) Arrangement.spacedBy(4.dp) else Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .dragToSelectGrid(
                            state = gridState,
                            topPaddingPx = topPaddingPx,
                            startPaddingPx = startPaddingPx,
                            onDragStart = { start ->
                                viewModel.isDragging = true
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.startDragSelect(start)
                            },
                            onDragFinished = {
                                viewModel.isDragging = false
                                viewModel.lastDragEndTime = System.currentTimeMillis()
                            },
                            onSelectRange = { start, end ->
                                viewModel.selectRange(start, end)
                            }
                        )
                ) {
                    items(uiState.files, key = { it.path }) { file ->
                        val iconContent = remember(file) {
                            movableContentOf {
                                FileIcon(
                                    file = file,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                        if (uiState.isGridView) {
                            FileGridItem(
                                file = file,
                                isSelected = uiState.selectedFiles.contains(file),
                                onClick = {
                                    if (viewModel.isDragging || System.currentTimeMillis() - viewModel.lastDragEndTime < 300) {
                                        return@FileGridItem
                                    }
                                    if (uiState.selectedFiles.isNotEmpty()) {
                                        viewModel.toggleSelection(file)
                                    } else {
                                        if (file.isDirectory) onNavigateToFolder(file)
                                        else onOpenFile(file)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleSelection(file)
                                },
                                iconContent = iconContent
                            )
                        } else {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                        val selectedJFiles = arrayListOf(file)
                                        Statics.prepareAction(DialogCopy(selectedJFiles))
                                        MainActivity.actionBarVisibility(View.VISIBLE)
                                    }
                                    false // Never actually dismiss the item from the UI
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromEndToStart = false,
                                enableDismissFromStartToEnd = true,
                                backgroundContent = {
                                    val color = MaterialTheme.colorScheme.primary
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.action_copy),
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            ) {
                                FileRowItem(
                                    file = file,
                                    isSelected = uiState.selectedFiles.contains(file),
                                    onClick = {
                                        if (viewModel.isDragging || System.currentTimeMillis() - viewModel.lastDragEndTime < 300) {
                                            return@FileRowItem
                                        }
                                        if (uiState.selectedFiles.isNotEmpty()) {
                                            viewModel.toggleSelection(file)
                                        } else {
                                            if (file.isDirectory) onNavigateToFolder(file)
                                            else onOpenFile(file)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(file)
                                    },
                                    iconContent = iconContent
                                )
                            }
                        }
                    }
                } // End of LazyVerticalGrid

                    FastScroller(
                        gridState = gridState,
                        items = uiState.files,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding() + 80.dp
                            ),
                        sortMode = currentSort
                    )
            }
        }
    }
}
}

@OptIn(DelicateCoroutinesApi::class)
fun Modifier.dragToSelectGrid(
    state: LazyGridState,
    topPaddingPx: Float,
    startPaddingPx: Float,
    onDragStart: (Int) -> Unit,
    onDragFinished: () -> Unit,
    onSelectRange: (Int, Int) -> Unit
): Modifier = composed {
    val currentTopPadding by rememberUpdatedState(topPaddingPx)
    val currentStartPadding by rememberUpdatedState(startPaddingPx)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    val currentOnSelectRange by rememberUpdatedState(onSelectRange)

    this.pointerInput(Unit) {
        var initialIndex: Int? = null
        var autoScrollJob: Job? = null
        var currentPointerPosition: Offset? = null
        
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                if (Statics.copyMode) return@detectDragGesturesAfterLongPress
                val item = state.layoutInfo.visibleItemsInfo.find {
                    val startY = it.offset.y + currentTopPadding
                    val startX = it.offset.x + currentStartPadding
                offset.x >= startX && offset.x <= startX + it.size.width &&
                offset.y >= startY && offset.y <= startY + it.size.height
            }
            initialIndex = item?.index
            initialIndex?.let { initIndext ->
                currentOnDragStart(initIndext)
                currentOnSelectRange(initIndext, initIndext)
                currentPointerPosition = offset
                
                autoScrollJob = GlobalScope.launch(Dispatchers.Main) {
                    while (isActive) {
                        currentPointerPosition?.let { pos ->
                            val topThreshold = 100.dp.toPx()
                            val bottomThreshold = size.height - 100.dp.toPx()
                            var scrollSpeed = 0f
                            
                            if (pos.y < topThreshold) {
                                val ratio = (topThreshold - pos.y).coerceAtLeast(0f) / topThreshold
                                scrollSpeed = -(ratio * 30f)
                            } else if (pos.y > bottomThreshold) {
                                val ratio = (pos.y - bottomThreshold).coerceAtLeast(0f) / (size.height - bottomThreshold)
                                scrollSpeed = (ratio * 30f)
                            }
                            
                            if (scrollSpeed != 0f) {
                                state.scrollBy(scrollSpeed)
                                val currentItem = state.layoutInfo.visibleItemsInfo.find {
                                    val startY = it.offset.y + currentTopPadding
                                    val startX = it.offset.x + currentStartPadding
                                    pos.x >= startX && pos.x <= startX + it.size.width &&
                                    pos.y >= startY && pos.y <= startY + it.size.height
                                }
                                currentItem?.index?.let { currentIdx ->
                                    currentOnSelectRange(initialIndex!!, currentIdx)
                                }
                            }
                        }
                        delay(16)
                    }
                }
            }
        },
        onDrag = { change, _ ->
            if (initialIndex == null) return@detectDragGesturesAfterLongPress
            currentPointerPosition = change.position
            val item = state.layoutInfo.visibleItemsInfo.find {
                val startY = it.offset.y + currentTopPadding
                val startX = it.offset.x + currentStartPadding
                change.position.x >= startX && change.position.x <= startX + it.size.width &&
                change.position.y >= startY && change.position.y <= startY + it.size.height
            }
            item?.index?.let { current ->
                currentOnSelectRange(initialIndex!!, current)
            }
        },
        onDragEnd = { 
            initialIndex = null 
            currentPointerPosition = null
            autoScrollJob?.cancel()
            currentOnDragFinished()
        },
        onDragCancel = { 
            initialIndex = null 
            currentPointerPosition = null
            autoScrollJob?.cancel()
            currentOnDragFinished()
        }
    )
}
}