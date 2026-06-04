package com.example.files.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PathBreadcrumbs(
    currentPath: String,
    onNavigate: (String) -> Unit,
    onHomeClick: () -> Unit
) {
    val rootDir = android.os.Environment.getExternalStorageDirectory().path
    
    val isInternal = currentPath.startsWith(rootDir)
    val relativePath = if (isInternal) {
        currentPath.substring(rootDir.length)
    } else {
        currentPath
    }
    
    val segments = relativePath.split("/").filter { it.isNotEmpty() }
    
    val segmentsWithPaths = buildList {
        add(Pair("Internal Storage", rootDir))
        
        var currentAccPath = rootDir
        for (segment in segments) {
            currentAccPath += "/$segment"
            add(Pair(segment, currentAccPath))
        }
    }
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(segmentsWithPaths.size) {
        if (segmentsWithPaths.isNotEmpty()) {
            listState.animateScrollToItem(segmentsWithPaths.size - 1)
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            LazyRow(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(segmentsWithPaths) { index, (name, path) ->
                    if (index > 0) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                    
                    var menuExpanded by remember { mutableStateOf(false) }
                    val isLast = index == segmentsWithPaths.lastIndex
                    
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .combinedClickable(
                                    onClick = { if (!isLast) onNavigate(path) },
                                    onLongClick = { menuExpanded = true }
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 15.sp,
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            var subDirs by remember { mutableStateOf<List<File>?>(null) }
                            
                            LaunchedEffect(menuExpanded) {
                                if (menuExpanded && subDirs == null) {
                                    withContext(Dispatchers.IO) {
                                        subDirs = File(path).listFiles { file -> file.isDirectory && !file.isHidden }
                                            ?.toList()?.sortedBy { it.name.lowercase() } ?: emptyList()
                                    }
                                }
                            }
                            
                            if (subDirs == null) {
                                DropdownMenuItem(
                                    text = { Text("Loading...") },
                                    onClick = { }
                                )
                            } else if (subDirs!!.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No sub-directories") },
                                    onClick = { menuExpanded = false }
                                )
                            } else {
                                subDirs!!.forEach { dir ->
                                    DropdownMenuItem(
                                        text = { Text(dir.name) },
                                        onClick = { 
                                            menuExpanded = false
                                            onNavigate(dir.path)
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
}
