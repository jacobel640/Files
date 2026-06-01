package com.example.files.utils

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.files.R
import com.example.files.models.JFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FileIcon(file: JFile, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var iconModel by remember(file) { mutableStateOf<Any?>(file.cachedIcon) }
    
    // Load the precise icon/art model asynchronously if it hasn't been cached yet.
    LaunchedEffect(file) {
        if (iconModel == null) {
            withContext(Dispatchers.IO) {
                // Returns byte[] for audio art, Drawable for APK, Uri for images/videos
                iconModel = file.loadIconInternal()
            }
        }
    }

    val typeResourceId = remember(file.type) { 
        when (file.type) {
            JFile.Type.FOLDER -> R.drawable.folder
            JFile.Type.AUDIO -> R.drawable.ctg_audio
            JFile.Type.IMAGE -> R.drawable.ctg_photo
            JFile.Type.VIDEO -> R.drawable.ctg_video
            JFile.Type.APK -> R.drawable.ext_apk
            JFile.Type.ARCHIVE -> R.drawable.ctg_archive
            JFile.Type.DOCUMENT -> R.drawable.type_office
            else -> R.drawable.file
        }
    }
    val isImageType = remember(file.type) { FileIcon.isImageType(file.type) }
    
    // Check if the loaded model is essentially "just a placeholder" and not real media.
    val isJustPlaceholder = iconModel is Drawable || iconModel == null || iconModel is Int

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // 1. Image Layer (Audio Art, Video, Image, APK icon)
        if (iconModel != null && iconModel !is Int) {
            GlideImage(
                model = iconModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize().padding(if (isImageType) 0.dp else 5.dp),
                contentScale = if (isImageType) ContentScale.Crop else ContentScale.Fit,
                loading = placeholder {
                    Image(
                        painter = painterResource(id = typeResourceId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().padding(5.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                failure = placeholder {
                    Image(
                        painter = painterResource(id = typeResourceId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().padding(5.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            )
        } else {
            // Fallback placeholder while loading or if it is just a drawable
            Image(
                painter = painterResource(id = typeResourceId),
                contentDescription = null,
                modifier = Modifier.matchParentSize().padding(5.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 2. Frame Border & Corner Indicator Overlay
        if (!isJustPlaceholder && isImageType) {
            // Frame overlay (slight gentle border)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            )

            // Tiny corner indicator for type (Photo, Video, Audio)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = typeResourceId),
                    contentDescription = "Type Indicator",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
