package com.example.files.components

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.files.Statics
import com.example.files.models.JFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FastScroller(
    gridState: LazyGridState,
    items: List<JFile>,
    modifier: Modifier = Modifier,
    sortMode: Int = Statics.sort
) {
    if (items.isEmpty()) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var containerHeight by remember { mutableFloatStateOf(0f) }
    val thumbHeight = 40.dp
    
    val totalItemsCount = gridState.layoutInfo.totalItemsCount
    if (totalItemsCount == 0) return
    
    val popupText by remember(items, sortMode) {
        derivedStateOf {
            val firstVisibleItemIndex = gridState.firstVisibleItemIndex
            if (firstVisibleItemIndex in items.indices) {
                val file = items[firstVisibleItemIndex]
                when (sortMode) {
                    0 -> file.name.take(1).uppercase()
                    1 -> Formatter.formatFileSize(context, file.size)
                    2 -> {
                        val span = DateUtils.getRelativeTimeSpanString(
                            file.lastModified(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_RELATIVE
                        ).toString()
                        if (span.contains("In") || span.contains("in")) "Just now" else span
                    }
                    3 -> if (file.isDirectory) "Folder" else file.extension.take(4).uppercase()
                    else -> file.name.take(1).uppercase()
                }
            } else ""
        }
    }

    val proportion = if (totalItemsCount > 1) {
        gridState.firstVisibleItemIndex.toFloat() / (totalItemsCount - 1)
    } else 0f
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(isDragging, gridState.isScrollInProgress) {
        if (isDragging || gridState.isScrollInProgress) {
            isVisible = true
        } else {
            delay(1500)
            isVisible = false
        }
    }
    
    val thumbAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, label = "thumbAlpha")
    val popupAlpha by animateFloatAsState(targetValue = if (isDragging) 1f else 0f, label = "popupAlpha")

    var dragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height.toFloat()
            }
    ) {
        // Restrict drag area to exactly where the thumb is placed
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(48.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val thumbHeightPx = thumbHeight.toPx()
                            val maxOffset = (containerHeight - thumbHeightPx).coerceAtLeast(0f)
                            
                            // Center the thumb on the touch offset, clamped to bounds
                            dragY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, maxOffset)
                            
                            // Scroll immediately to the new position
                            val targetProportion = if (maxOffset > 0) dragY / maxOffset else 0f
                            val targetIndex = (targetProportion * totalItemsCount).toInt().coerceIn(0, totalItemsCount - 1)
                            coroutineScope.launch {
                                gridState.scrollToItem(targetIndex)
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val thumbHeightPx = thumbHeight.toPx()
                            val maxOffset = (containerHeight - thumbHeightPx).coerceAtLeast(0f)
                            dragY += dragAmount.y
                            val clampedY = dragY.coerceIn(0f, maxOffset)
                            val targetProportion = if (maxOffset > 0) clampedY / maxOffset else 0f
                            val targetIndex = (targetProportion * totalItemsCount).toInt().coerceIn(0, totalItemsCount - 1)
                            coroutineScope.launch {
                                gridState.scrollToItem(targetIndex)
                            }
                        }
                    )
                }
        )

        val thumbHeightPx = with(context.resources.displayMetrics) { thumbHeight.value * density }
        val maxOffset = (containerHeight - thumbHeightPx).coerceAtLeast(0f)
        val yOffset = if (isDragging) {
            dragY.coerceIn(0f, maxOffset)
        } else {
            (maxOffset * proportion).coerceIn(0f, maxOffset)
        }
        
        if (thumbAlpha > 0f) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, yOffset.roundToInt()) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (popupAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .alpha(popupAlpha)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = popupText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Box(
                    modifier = Modifier
                        .alpha(thumbAlpha)
                        .size(8.dp, thumbHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
