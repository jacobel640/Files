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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Rounded.ExtTypeOffice: ImageVector
    @Composable
    get() {
        val current = _extTypeOffice
        if (current != null) return current

        return ImageVector.Builder(
            name = "MaterialTheme.ExtTypeOffice",
            defaultWidth = 144.0.dp,
            defaultHeight = 144.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            // M35.883 7.341 C37.726 7.85 39 9.508 39 11.397 v25.162 c0 1.906 -1.301 3.57 -3.168 4.065 L24.29 43.863 L28 36 V11 l-3.148 -6.885 L35.883 7.341z
            path(
                fill = Brush.linearGradient(
                    0f to Color(0xFFE68E00),
                    0.036f to Color(0xFFE38400),
                    0.171f to Color(0xFFDB6200),
                    0.299f to Color(0xFFD44A00),
                    0.417f to Color(0xFFD03B00),
                    0.515f to Color(0xFFCF3600),
                    0.878f to Color(0xFFD22900),
                    1f to Color(0xFFD42400),
                    start = Offset(x = 31.645f, y = 6.839f),
                    end = Offset(x = 31.645f, y = 40.615f),
                ),
            ) {
                // M 35.883 7.341
                moveTo(x = 35.883f, y = 7.341f)
                // C 37.726 7.85 39 9.508 39 11.397
                curveTo(
                    x1 = 37.726f,
                    y1 = 7.85f,
                    x2 = 39.0f,
                    y2 = 9.508f,
                    x3 = 39.0f,
                    y3 = 11.397f,
                )
                // v 25.162
                verticalLineToRelative(dy = 25.162f)
                // c 0 1.906 -1.301 3.57 -3.168 4.065
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.906f,
                    dx2 = -1.301f,
                    dy2 = 3.57f,
                    dx3 = -3.168f,
                    dy3 = 4.065f,
                )
                // L 24.29 43.863
                lineTo(x = 24.29f, y = 43.863f)
                // L 28 36
                lineTo(x = 28.0f, y = 36.0f)
                // V 11
                verticalLineTo(y = 11.0f)
                // l -3.148 -6.885
                lineToRelative(dx = -3.148f, dy = -6.885f)
                // L 35.883 7.341z
                lineTo(x = 35.883f, y = 7.341f)
                close()
            }
            // M28 35 v3.927 c0 3.803 -3.824 6.249 -7.019 4.491 l-6.936 -4.445 c-0.802 -0.466 -1.236 -1.462 -0.964 -2.457 C13.334 35.59 14.202 35 15.115 35 H28z
            path(
                fill = Brush.linearGradient(
                    0f to Color(0xFFF52537),
                    0.293f to Color(0xFFF32536),
                    0.465f to Color(0xFFEA2434),
                    0.605f to Color(0xFFDC2231),
                    0.729f to Color(0xFFC8202C),
                    0.841f to Color(0xFFAE1E25),
                    0.944f to Color(0xFF8F1A1D),
                    1f to Color(0xFF7A1818),
                    start = Offset(x = 13.922f, y = 34.951f),
                    end = Offset(x = 29.051f, y = 41.073f),
                ),
            ) {
                // M 28 35
                moveTo(x = 28.0f, y = 35.0f)
                // v 3.927
                verticalLineToRelative(dy = 3.927f)
                // c 0 3.803 -3.824 6.249 -7.019 4.491
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 3.803f,
                    dx2 = -3.824f,
                    dy2 = 6.249f,
                    dx3 = -7.019f,
                    dy3 = 4.491f,
                )
                // l -6.936 -4.445
                lineToRelative(dx = -6.936f, dy = -4.445f)
                // c -0.802 -0.466 -1.236 -1.462 -0.964 -2.457
                curveToRelative(
                    dx1 = -0.802f,
                    dy1 = -0.466f,
                    dx2 = -1.236f,
                    dy2 = -1.462f,
                    dx3 = -0.964f,
                    dy3 = -2.457f,
                )
                // C 13.334 35.59 14.202 35 15.115 35
                curveTo(
                    x1 = 13.334f,
                    y1 = 35.59f,
                    x2 = 14.202f,
                    y2 = 35.0f,
                    x3 = 15.115f,
                    y3 = 35.0f,
                )
                // H 28z
                horizontalLineTo(x = 28.0f)
                close()
            }
            // M21.946 4.526 l-11.924 6.786 C8.772 12.024 8 13.351 8 14.789 v18.429 c0 1.357 1.459 2.215 2.645 1.554 l4.472 -2.491 C15.662 31.978 16 31.402 16 30.778 V17.743 c0 -1.307 0.78 -2.48 1.963 -2.949 L28 11.308 v-3.09 C28 5.014 24.669 2.983 21.946 4.526z
            path(
                fill = Brush.linearGradient(
                    0f to Color(0xFFD96AB1),
                    0.137f to Color(0xFFD9538B),
                    0.495f to Color(0xFFD91A2A),
                    0.575f to Color(0xFFD31A29),
                    0.68f to Color(0xFFC21926),
                    0.8f to Color(0xFFA71821),
                    0.929f to Color(0xFF811619),
                    1f to Color(0xFF691515),
                    start = Offset(x = 5.382f, y = 32.289f),
                    end = Offset(x = 25.874f, y = 1.78f),
                ),
            ) {
                // M 21.946 4.526
                moveTo(x = 21.946f, y = 4.526f)
                // l -11.924 6.786
                lineToRelative(dx = -11.924f, dy = 6.786f)
                // C 8.772 12.024 8 13.351 8 14.789
                curveTo(
                    x1 = 8.772f,
                    y1 = 12.024f,
                    x2 = 8.0f,
                    y2 = 13.351f,
                    x3 = 8.0f,
                    y3 = 14.789f,
                )
                // v 18.429
                verticalLineToRelative(dy = 18.429f)
                // c 0 1.357 1.459 2.215 2.645 1.554
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.357f,
                    dx2 = 1.459f,
                    dy2 = 2.215f,
                    dx3 = 2.645f,
                    dy3 = 1.554f,
                )
                // l 4.472 -2.491
                lineToRelative(dx = 4.472f, dy = -2.491f)
                // C 15.662 31.978 16 31.402 16 30.778
                curveTo(
                    x1 = 15.662f,
                    y1 = 31.978f,
                    x2 = 16.0f,
                    y2 = 31.402f,
                    x3 = 16.0f,
                    y3 = 30.778f,
                )
                // V 17.743
                verticalLineTo(y = 17.743f)
                // c 0 -1.307 0.78 -2.48 1.963 -2.949
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.307f,
                    dx2 = 0.78f,
                    dy2 = -2.48f,
                    dx3 = 1.963f,
                    dy3 = -2.949f,
                )
                // L 28 11.308
                lineTo(x = 28.0f, y = 11.308f)
                // v -3.09
                verticalLineToRelative(dy = -3.09f)
                // C 28 5.014 24.669 2.983 21.946 4.526z
                curveTo(
                    x1 = 28.0f,
                    y1 = 5.014f,
                    x2 = 24.669f,
                    y2 = 2.983f,
                    x3 = 21.946f,
                    y3 = 4.526f,
                )
                close()
            }
        }.build().also { _extTypeOffice = it }
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
                imageVector = Icons.Rounded.ExtTypeOffice,
                contentDescription = null,
                modifier = Modifier
                    .width((144.0).dp)
                    .height((144.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _extTypeOffice: ImageVector? = null
