package com.example.files.components

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.files.Statics
import com.example.files.listeners.OnSizeLoadReady
import com.example.files.models.JFile

import androidx.compose.ui.platform.LocalContext

@Composable
fun FileSizeLabel(file: JFile, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.End) {
    val context = LocalContext.current
    if (!Statics.showFileSize || !file.isDirectory) {
        Text(
            text = file.stringSize ?: "",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
            textAlign = textAlign
        )
        return
    }

    var sizeLabel by remember(file) { mutableStateOf(if (file.isSizeReady) Formatter.formatFileSize(context, file.size) else "pending") }
    var isLoading by remember(file) { mutableStateOf(!file.isSizeReady) }

    DisposableEffect(file) {
        val listener = object : OnSizeLoadReady {
            override fun onSizeUpdate(size: Long) {
                sizeLabel = Formatter.formatFileSize(context, size)
                isLoading = true
            }

            override fun onSizeReady(size: Long) {
                sizeLabel = Formatter.formatFileSize(context, size)
                isLoading = false
            }
        }
        file.setSizeLoadListener(listener)
        file.loadSizeIfNeeded()
        isLoading = file.isSizeLoading

        onDispose {
            file.setSizeLoadListener(null)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = 0.5f
                            scaleY = 0.5f
                        },
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = sizeLabel,
            fontSize = 12.sp,
            color = if (sizeLabel == "pending") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign
        )
    }
}
