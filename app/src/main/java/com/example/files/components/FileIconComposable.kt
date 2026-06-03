package com.example.files.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.files.R
import com.example.files.models.JFile
import com.example.files.utils.FileIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FileIcon(file: JFile, modifier: Modifier = Modifier) {
    var iconModel by remember(file) { mutableStateOf<Any?>(file.cachedIcon) }
    
    // Load the precise icon/art model asynchronously if it hasn't been cached yet.
    LaunchedEffect(file) {
        if (iconModel == null) {
            withContext(Dispatchers.IO) {
                // Returns byte[] for audio art, Drawable for APK, Uri for images/videos
                val loaded = file.loadIconInternal()
                file.cachedIcon = loaded
                iconModel = loaded
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

    var isImageLoaded by remember(iconModel) { mutableStateOf(false) }

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
                requestBuilderTransform = {
                    it.addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            isImageLoaded = true
                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            isImageLoaded = true
                            return false
                        }
                    })
                },
                loading = placeholder {
                    Image(
                        painter = painterResource(id = typeResourceId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().padding(if (isImageType) 5.dp else 0.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                failure = placeholder {
                    Image(
                        painter = painterResource(id = typeResourceId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().padding(if (isImageType) 5.dp else 0.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            )
        } else {
            // Fallback placeholder while loading or if it is just a drawable/int
            Image(
                painter = painterResource(id = typeResourceId),
                contentDescription = null,
                modifier = Modifier.matchParentSize().padding(5.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 2. Frame Border & Corner Indicator Overlay
        if (!isJustPlaceholder && isImageType && isImageLoaded) {
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
