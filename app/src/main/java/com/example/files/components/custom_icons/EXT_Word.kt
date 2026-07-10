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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.files.R
import iconBackground

val Icons.Rounded.ExtWord: ImageVector
    @Composable
    get() {
        val current = _extWord
        if (current != null) return current

        val background = MaterialTheme.colorScheme.iconBackground()

        return ImageVector.Builder(
            name = "MaterialTheme.ExtWord",
            defaultWidth = 48.0.dp,
            defaultHeight = 48.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            group(
                translationX = 0.0f,
                translationY = 0.0f,
            ) {
                // M42.256 6 H15.744 C14.781 6 14 6.781 14 7.744 v7.259 h30 V7.744 C44 6.781 43.219 6 42.256 6z
                path(
                    fill = SolidColor(colorResource(R.color.ext_excel_2)),
                ) {
                    // M 42.256 6
                    moveTo(x = 42.256f, y = 6.0f)
                    // H 15.744
                    horizontalLineTo(x = 15.744f)
                    // C 14.781 6 14 6.781 14 7.744
                    curveTo(
                        x1 = 14.781f,
                        y1 = 6.0f,
                        x2 = 14.0f,
                        y2 = 6.781f,
                        x3 = 14.0f,
                        y3 = 7.744f,
                    )
                    // v 7.259
                    verticalLineToRelative(dy = 7.259f)
                    // h 30
                    horizontalLineToRelative(dx = 30.0f)
                    // V 7.744
                    verticalLineTo(y = 7.744f)
                    // C 44 6.781 43.219 6 42.256 6z
                    curveTo(
                        x1 = 44.0f,
                        y1 = 6.781f,
                        x2 = 43.219f,
                        y2 = 6.0f,
                        x3 = 42.256f,
                        y3 = 6.0f,
                    )
                    close()
                }
                // M14 15.003 H44 V24.005000000000003 H14z
                path(
                    fill = SolidColor(colorResource(R.color.ext_excel_3)),
                ) {
                    // M 14 15.003
                    moveTo(x = 14.0f, y = 15.003f)
                    // H 44
                    horizontalLineTo(x = 44.0f)
                    // V 24.005000000000003
                    verticalLineTo(y = 24.005000000000003f)
                    // H 14z
                    horizontalLineTo(x = 14.0f)
                    close()
                }
                // M14 24.005 H44 V33.055 H14z
                path(
                    fill = SolidColor(colorResource(R.color.ext_excel_5)),
                ) {
                    // M 14 24.005
                    moveTo(x = 14.0f, y = 24.005f)
                    // H 44
                    horizontalLineTo(x = 44.0f)
                    // V 33.055
                    verticalLineTo(y = 33.055f)
                    // H 14z
                    horizontalLineTo(x = 14.0f)
                    close()
                }
                // M14 33.054 v7.202 C14 41.219 14.781 42 15.743 42 h26.513 C43.219 42 44 41.219 44 40.256 v-7.202 H14z
                path(
                    fill = SolidColor(colorResource(R.color.ext_excel_6)),
                ) {
                    // M 14 33.054
                    moveTo(x = 14.0f, y = 33.054f)
                    // v 7.202
                    verticalLineToRelative(dy = 7.202f)
                    // C 14 41.219 14.781 42 15.743 42
                    curveTo(
                        x1 = 14.0f,
                        y1 = 41.219f,
                        x2 = 14.781f,
                        y2 = 42.0f,
                        x3 = 15.743f,
                        y3 = 42.0f,
                    )
                    // h 26.513
                    horizontalLineToRelative(dx = 26.513f)
                    // C 43.219 42 44 41.219 44 40.256
                    curveTo(
                        x1 = 43.219f,
                        y1 = 42.0f,
                        x2 = 44.0f,
                        y2 = 41.219f,
                        x3 = 44.0f,
                        y3 = 40.256f,
                    )
                    // v -7.202
                    verticalLineToRelative(dy = -7.202f)
                    // H 14z
                    horizontalLineTo(x = 14.0f)
                    close()
                }
                // M22.319 34 H5.681 C4.753 34 4 33.247 4 32.319 V15.681 C4 14.753 4.753 14 5.681 14 h16.638 C23.247 14 24 14.753 24 15.681 v16.638 C24 33.247 23.247 34 22.319 34z
                path(
                    fill = SolidColor(colorResource(R.color.ext_excel_badge)),
                ) {
                    // M 22.319 34
                    moveTo(x = 22.319f, y = 34.0f)
                    // H 5.681
                    horizontalLineTo(x = 5.681f)
                    // C 4.753 34 4 33.247 4 32.319
                    curveTo(
                        x1 = 4.753f,
                        y1 = 34.0f,
                        x2 = 4.0f,
                        y2 = 33.247f,
                        x3 = 4.0f,
                        y3 = 32.319f,
                    )
                    // V 15.681
                    verticalLineTo(y = 15.681f)
                    // C 4 14.753 4.753 14 5.681 14
                    curveTo(
                        x1 = 4.0f,
                        y1 = 14.753f,
                        x2 = 4.753f,
                        y2 = 14.0f,
                        x3 = 5.681f,
                        y3 = 14.0f,
                    )
                    // h 16.638
                    horizontalLineToRelative(dx = 16.638f)
                    // C 23.247 14 24 14.753 24 15.681
                    curveTo(
                        x1 = 23.247f,
                        y1 = 14.0f,
                        x2 = 24.0f,
                        y2 = 14.753f,
                        x3 = 24.0f,
                        y3 = 15.681f,
                    )
                    // v 16.638
                    verticalLineToRelative(dy = 16.638f)
                    // C 24 33.247 23.247 34 22.319 34z
                    curveTo(
                        x1 = 24.0f,
                        y1 = 33.247f,
                        x2 = 23.247f,
                        y2 = 34.0f,
                        x3 = 22.319f,
                        y3 = 34.0f,
                    )
                    close()
                }
                // M18.403 19 L16.857 26.264 15.144 19 12.957 19 11.19 26.489 9.597 19 7.641 19 9.985 29 12.337 29 14.05 21.311 15.764 29 18.015 29 20.359 19z
                path(
                    fill = SolidColor(background),
                ) {
                    // M 18.403 19
                    moveTo(x = 18.403f, y = 19.0f)
                    // L 16.857 26.264
                    lineTo(x = 16.857f, y = 26.264f)
                    // L 15.144 19
                    lineTo(x = 15.144f, y = 19.0f)
                    // L 12.957 19
                    lineTo(x = 12.957f, y = 19.0f)
                    // L 11.19 26.489
                    lineTo(x = 11.19f, y = 26.489f)
                    // L 9.597 19
                    lineTo(x = 9.597f, y = 19.0f)
                    // L 7.641 19
                    lineTo(x = 7.641f, y = 19.0f)
                    // L 9.985 29
                    lineTo(x = 9.985f, y = 29.0f)
                    // L 12.337 29
                    lineTo(x = 12.337f, y = 29.0f)
                    // L 14.05 21.311
                    lineTo(x = 14.05f, y = 21.311f)
                    // L 15.764 29
                    lineTo(x = 15.764f, y = 29.0f)
                    // L 18.015 29
                    lineTo(x = 18.015f, y = 29.0f)
                    // L 20.359 19z
                    lineTo(x = 20.359f, y = 19.0f)
                    close()
                }
            }
        }.build().also { _extWord = it }
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
                imageVector = Icons.Rounded.ExtWord,
                contentDescription = null,
                modifier = Modifier
                    .width((48.0).dp)
                    .height((48.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _extWord: ImageVector? = null
