package com.example.files.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.files.MainActivity
import com.example.files.R
import com.example.files.Statics
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaskState(val action: com.example.files.actions.DialogBase) {
    var isFinished by mutableStateOf(false)
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ActiveTasksOverlay() {
    var expanded by remember { mutableStateOf(false) }
    val taskItems = remember { mutableStateListOf<TaskState>() }
    val context = LocalContext.current as? MainActivity

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val currentActions = Statics.actions.toList()
            
            // Add new active tasks
            currentActions.forEach { action ->
                if (action.operating && taskItems.none { it.action == action }) {
                    taskItems.add(TaskState(action))
                }
            }
            
            // Mark finished tasks and schedule removal
            val iterator = taskItems.iterator()
            for (task in iterator) {
                if (!task.isFinished && !currentActions.contains(task.action)) {
                    task.isFinished = true
                    // Launch a coroutine to remove it after 1.5s
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        delay(1500)
                        taskItems.remove(task)
                    }
                }
            }
        }
    }

    if (taskItems.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .padding(8.dp, 10.dp)
                .fillMaxWidth(0.9f)
                .clickable { expanded = !expanded }
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Rounded.Task,
                            contentDescription = "Active Tasks",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${taskItems.size} Active Task${if (taskItems.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { }
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(taskItems, key = { it.action.hashCode() }) { task ->
                            TaskProgressItem(task, context)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun TaskProgressItem(task: TaskState, context: MainActivity?) {
    val titleRes = task.action.resTitle
    val title = if (titleRes != 0 && context != null) context.getString(titleRes) else "Running"
    val idText = (task.action.dialogID + 1).toString()
    
    Box(
        modifier = Modifier
            .size(50.dp, 50.dp)
            .background(
                color = colorResource(id = R.color.selected),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                if (!task.isFinished) task.action.reshow()
            }
    ) {
        if (task.isFinished) {
            val avd = AnimatedImageVector.animatedVectorResource(R.drawable.avd_check_uncheck)
            var atEnd by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                atEnd = true
            }
            Icon(
                painter = rememberAnimatedVectorPainter(avd, atEnd = atEnd),
                contentDescription = "Finished",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = idText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = title, fontSize = 10.sp, maxLines = 1)
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .height(2.dp),
                color = colorResource(id = R.color.app_theme),
                trackColor = colorResource(id = R.color.divider)
            )
        }
    }
}

fun bindActiveTasksOverlay(composeView: androidx.compose.ui.platform.ComposeView) {
    composeView.setContent {
        ActiveTasksOverlay()
    }
}
