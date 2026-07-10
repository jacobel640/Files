package com.example.files.components.custom_icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import iconPrimary

val Icons.Rounded.ExtFolder: ImageVector
    @Composable
    get() {
        val current = _extFolder
        if (current != null) return current

        val primary = MaterialTheme.colorScheme.iconPrimary()

        return ImageVector.Builder(
            name = "CustomVector",
            defaultWidth = 200.dp, // android:width="200dp"
            defaultHeight = 200.dp, // android:height="200dp"
            viewportWidth = 500f,   // android:viewportWidth="500"
            viewportHeight = 500f   // android:viewportHeight="500"
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(primary), // android:strokeColor
                strokeLineWidth = 30f              // android:strokeWidth="30"
            ) {
                // android:pathData המרה מדויקת של
                moveTo(58.95f, 212.796f)
                lineTo(58.95f, 142.549f)
                curveTo(58.95f, 106.234f, 88.389f, 76.796f, 124.703f, 76.796f)
                lineTo(176.248f, 76.796f)
                curveTo(187.912f, 76.796f, 198.786f, 82.696f, 205.143f, 92.476f)
                lineTo(210.898f, 101.328f)
                curveTo(217.916f, 112.125f, 229.919f, 118.638f, 242.796f, 118.638f)
                lineTo(369.788f, 118.638f)
                curveTo(408.427f, 118.638f, 439.75f, 149.961f, 439.75f, 188.6f)
                lineTo(439.75f, 353.537f)
                curveTo(439.75f, 392.709f, 407.995f, 424.464f, 368.822f, 424.464f)
                lineTo(130.38f, 424.464f)
                curveTo(90.93f, 424.464f, 58.95f, 392.484f, 58.95f, 353.035f)
                lineTo(58.95f, 267.196f)
                close() // פקודת Z בסוף הנתיב
            }
        }.build().also { _extFolder = it }
    }

@Preview
@Composable
private fun IconPreview() {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = Icons.Rounded.ExtFolder,
                contentDescription = null,
                modifier = Modifier
                    .width((200.0).dp)
                    .height((200.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _extFolder: ImageVector? = null
