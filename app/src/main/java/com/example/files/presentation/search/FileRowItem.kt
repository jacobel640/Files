package com.example.files.presentation.search

import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.files.Statics
import com.example.files.components.FileIcon
import com.example.files.models.JFile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRowItem(file: JFile) {
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (file.isDirectory) {
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
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            FileIcon(
                file = file,
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (Statics.isSingleLine) 1 else Int.MAX_VALUE,
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
        HorizontalDivider(
            modifier = Modifier.padding(start = 82.dp, end = 5.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}