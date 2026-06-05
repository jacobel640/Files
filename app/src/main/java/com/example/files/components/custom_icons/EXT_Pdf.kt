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

val Icons.Rounded.ExtPdf: ImageVector
    get() {
        val current = _extPdf
        if (current != null) return current

        return ImageVector.Builder(
            name = "MaterialTheme.ExtPdf",
            defaultWidth = 48.0.dp,
            defaultHeight = 48.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            // M38 42 H10 c-2.209 0 -4 -1.791 -4 -4 V10 c0 -2.209 1.791 -4 4 -4 h28 c2.209 0 4 1.791 4 4 v28  C42 40.209 40.209 42 38 42z
            path(
                fill = SolidColor(Color(0xFFE53935)),
            ) {
                // M 38 42
                moveTo(x = 38.0f, y = 42.0f)
                // H 10
                horizontalLineTo(x = 10.0f)
                // c -2.209 0 -4 -1.791 -4 -4
                curveToRelative(
                    dx1 = -2.209f,
                    dy1 = 0.0f,
                    dx2 = -4.0f,
                    dy2 = -1.791f,
                    dx3 = -4.0f,
                    dy3 = -4.0f,
                )
                // V 10
                verticalLineTo(y = 10.0f)
                // c 0 -2.209 1.791 -4 4 -4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.209f,
                    dx2 = 1.791f,
                    dy2 = -4.0f,
                    dx3 = 4.0f,
                    dy3 = -4.0f,
                )
                // h 28
                horizontalLineToRelative(dx = 28.0f)
                // c 2.209 0 4 1.791 4 4
                curveToRelative(
                    dx1 = 2.209f,
                    dy1 = 0.0f,
                    dx2 = 4.0f,
                    dy2 = 1.791f,
                    dx3 = 4.0f,
                    dy3 = 4.0f,
                )
                // v 28
                verticalLineToRelative(dy = 28.0f)
                // C 42 40.209 40.209 42 38 42z
                curveTo(
                    x1 = 42.0f,
                    y1 = 40.209f,
                    x2 = 40.209f,
                    y2 = 42.0f,
                    x3 = 38.0f,
                    y3 = 42.0f,
                )
                close()
            }
            // M34.841 26.799 c-1.692 -1.757 -6.314 -1.041 -7.42 -0.911 c-1.627 -1.562 -2.734 -3.45 -3.124 -4.101  c0.586 -1.757 0.976 -3.515 1.041 -5.402 c0 -1.627 -0.651 -3.385 -2.473 -3.385 c-0.651 0 -1.237 0.391 -1.562 0.911  c-0.781 1.367 -0.456 4.101 0.781 6.899 c-0.716 2.018 -1.367 3.97 -3.189 7.42 c-1.888 0.781 -5.858 2.604 -6.183 4.556  c-0.13 0.586 0.065 1.172 0.521 1.627 C13.688 34.805 14.273 35 14.859 35 c2.408 0 4.751 -3.32 6.379 -6.118  c1.367 -0.456 3.515 -1.107 5.663 -1.497 c2.538 2.213 4.751 2.538 5.923 2.538 c1.562 0 2.148 -0.651 2.343 -1.237  C35.492 28.036 35.297 27.32 34.841 26.799z M33.214 27.905 c-0.065 0.456 -0.651 0.911 -1.692 0.651  c-1.237 -0.325 -2.343 -0.911 -3.32 -1.692 c0.846 -0.13 2.734 -0.325 4.101 -0.065 C32.824 26.929 33.344 27.254 33.214 27.905z M22.344 14.497 c0.13 -0.195 0.325 -0.325 0.521 -0.325 c0.586 0 0.716 0.716 0.716 1.302 c-0.065 1.367 -0.325 2.734 -0.781 4.036  C21.824 16.905 22.019 15.083 22.344 14.497z M22.214 27.124 c0.521 -1.041 1.237 -2.864 1.497 -3.645  c0.586 0.976 1.562 2.148 2.083 2.669 C25.794 26.213 23.776 26.604 22.214 27.124z M18.374 29.728  c-1.497 2.473 -3.059 4.036 -3.905 4.036 c-0.13 0 -0.26 -0.065 -0.391 -0.13 c-0.195 -0.13 -0.26 -0.325 -0.195 -0.586  C14.078 32.136 15.77 30.899 18.374 29.728z
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                // M 34.841 26.799
                moveTo(x = 34.841f, y = 26.799f)
                // c -1.692 -1.757 -6.314 -1.041 -7.42 -0.911
                curveToRelative(
                    dx1 = -1.692f,
                    dy1 = -1.757f,
                    dx2 = -6.314f,
                    dy2 = -1.041f,
                    dx3 = -7.42f,
                    dy3 = -0.911f,
                )
                // c -1.627 -1.562 -2.734 -3.45 -3.124 -4.101
                curveToRelative(
                    dx1 = -1.627f,
                    dy1 = -1.562f,
                    dx2 = -2.734f,
                    dy2 = -3.45f,
                    dx3 = -3.124f,
                    dy3 = -4.101f,
                )
                // c 0.586 -1.757 0.976 -3.515 1.041 -5.402
                curveToRelative(
                    dx1 = 0.586f,
                    dy1 = -1.757f,
                    dx2 = 0.976f,
                    dy2 = -3.515f,
                    dx3 = 1.041f,
                    dy3 = -5.402f,
                )
                // c 0 -1.627 -0.651 -3.385 -2.473 -3.385
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.627f,
                    dx2 = -0.651f,
                    dy2 = -3.385f,
                    dx3 = -2.473f,
                    dy3 = -3.385f,
                )
                // c -0.651 0 -1.237 0.391 -1.562 0.911
                curveToRelative(
                    dx1 = -0.651f,
                    dy1 = 0.0f,
                    dx2 = -1.237f,
                    dy2 = 0.391f,
                    dx3 = -1.562f,
                    dy3 = 0.911f,
                )
                // c -0.781 1.367 -0.456 4.101 0.781 6.899
                curveToRelative(
                    dx1 = -0.781f,
                    dy1 = 1.367f,
                    dx2 = -0.456f,
                    dy2 = 4.101f,
                    dx3 = 0.781f,
                    dy3 = 6.899f,
                )
                // c -0.716 2.018 -1.367 3.97 -3.189 7.42
                curveToRelative(
                    dx1 = -0.716f,
                    dy1 = 2.018f,
                    dx2 = -1.367f,
                    dy2 = 3.97f,
                    dx3 = -3.189f,
                    dy3 = 7.42f,
                )
                // c -1.888 0.781 -5.858 2.604 -6.183 4.556
                curveToRelative(
                    dx1 = -1.888f,
                    dy1 = 0.781f,
                    dx2 = -5.858f,
                    dy2 = 2.604f,
                    dx3 = -6.183f,
                    dy3 = 4.556f,
                )
                // c -0.13 0.586 0.065 1.172 0.521 1.627
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = 0.586f,
                    dx2 = 0.065f,
                    dy2 = 1.172f,
                    dx3 = 0.521f,
                    dy3 = 1.627f,
                )
                // C 13.688 34.805 14.273 35 14.859 35
                curveTo(
                    x1 = 13.688f,
                    y1 = 34.805f,
                    x2 = 14.273f,
                    y2 = 35.0f,
                    x3 = 14.859f,
                    y3 = 35.0f,
                )
                // c 2.408 0 4.751 -3.32 6.379 -6.118
                curveToRelative(
                    dx1 = 2.408f,
                    dy1 = 0.0f,
                    dx2 = 4.751f,
                    dy2 = -3.32f,
                    dx3 = 6.379f,
                    dy3 = -6.118f,
                )
                // c 1.367 -0.456 3.515 -1.107 5.663 -1.497
                curveToRelative(
                    dx1 = 1.367f,
                    dy1 = -0.456f,
                    dx2 = 3.515f,
                    dy2 = -1.107f,
                    dx3 = 5.663f,
                    dy3 = -1.497f,
                )
                // c 2.538 2.213 4.751 2.538 5.923 2.538
                curveToRelative(
                    dx1 = 2.538f,
                    dy1 = 2.213f,
                    dx2 = 4.751f,
                    dy2 = 2.538f,
                    dx3 = 5.923f,
                    dy3 = 2.538f,
                )
                // c 1.562 0 2.148 -0.651 2.343 -1.237
                curveToRelative(
                    dx1 = 1.562f,
                    dy1 = 0.0f,
                    dx2 = 2.148f,
                    dy2 = -0.651f,
                    dx3 = 2.343f,
                    dy3 = -1.237f,
                )
                // C 35.492 28.036 35.297 27.32 34.841 26.799z
                curveTo(
                    x1 = 35.492f,
                    y1 = 28.036f,
                    x2 = 35.297f,
                    y2 = 27.32f,
                    x3 = 34.841f,
                    y3 = 26.799f,
                )
                close()
                // M 33.214 27.905
                moveTo(x = 33.214f, y = 27.905f)
                // c -0.065 0.456 -0.651 0.911 -1.692 0.651
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = 0.456f,
                    dx2 = -0.651f,
                    dy2 = 0.911f,
                    dx3 = -1.692f,
                    dy3 = 0.651f,
                )
                // c -1.237 -0.325 -2.343 -0.911 -3.32 -1.692
                curveToRelative(
                    dx1 = -1.237f,
                    dy1 = -0.325f,
                    dx2 = -2.343f,
                    dy2 = -0.911f,
                    dx3 = -3.32f,
                    dy3 = -1.692f,
                )
                // c 0.846 -0.13 2.734 -0.325 4.101 -0.065
                curveToRelative(
                    dx1 = 0.846f,
                    dy1 = -0.13f,
                    dx2 = 2.734f,
                    dy2 = -0.325f,
                    dx3 = 4.101f,
                    dy3 = -0.065f,
                )
                // C 32.824 26.929 33.344 27.254 33.214 27.905z
                curveTo(
                    x1 = 32.824f,
                    y1 = 26.929f,
                    x2 = 33.344f,
                    y2 = 27.254f,
                    x3 = 33.214f,
                    y3 = 27.905f,
                )
                close()
                // M 22.344 14.497
                moveTo(x = 22.344f, y = 14.497f)
                // c 0.13 -0.195 0.325 -0.325 0.521 -0.325
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = -0.195f,
                    dx2 = 0.325f,
                    dy2 = -0.325f,
                    dx3 = 0.521f,
                    dy3 = -0.325f,
                )
                // c 0.586 0 0.716 0.716 0.716 1.302
                curveToRelative(
                    dx1 = 0.586f,
                    dy1 = 0.0f,
                    dx2 = 0.716f,
                    dy2 = 0.716f,
                    dx3 = 0.716f,
                    dy3 = 1.302f,
                )
                // c -0.065 1.367 -0.325 2.734 -0.781 4.036
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = 1.367f,
                    dx2 = -0.325f,
                    dy2 = 2.734f,
                    dx3 = -0.781f,
                    dy3 = 4.036f,
                )
                // C 21.824 16.905 22.019 15.083 22.344 14.497z
                curveTo(
                    x1 = 21.824f,
                    y1 = 16.905f,
                    x2 = 22.019f,
                    y2 = 15.083f,
                    x3 = 22.344f,
                    y3 = 14.497f,
                )
                close()
                // M 22.214 27.124
                moveTo(x = 22.214f, y = 27.124f)
                // c 0.521 -1.041 1.237 -2.864 1.497 -3.645
                curveToRelative(
                    dx1 = 0.521f,
                    dy1 = -1.041f,
                    dx2 = 1.237f,
                    dy2 = -2.864f,
                    dx3 = 1.497f,
                    dy3 = -3.645f,
                )
                // c 0.586 0.976 1.562 2.148 2.083 2.669
                curveToRelative(
                    dx1 = 0.586f,
                    dy1 = 0.976f,
                    dx2 = 1.562f,
                    dy2 = 2.148f,
                    dx3 = 2.083f,
                    dy3 = 2.669f,
                )
                // C 25.794 26.213 23.776 26.604 22.214 27.124z
                curveTo(
                    x1 = 25.794f,
                    y1 = 26.213f,
                    x2 = 23.776f,
                    y2 = 26.604f,
                    x3 = 22.214f,
                    y3 = 27.124f,
                )
                close()
                // M 18.374 29.728
                moveTo(x = 18.374f, y = 29.728f)
                // c -1.497 2.473 -3.059 4.036 -3.905 4.036
                curveToRelative(
                    dx1 = -1.497f,
                    dy1 = 2.473f,
                    dx2 = -3.059f,
                    dy2 = 4.036f,
                    dx3 = -3.905f,
                    dy3 = 4.036f,
                )
                // c -0.13 0 -0.26 -0.065 -0.391 -0.13
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = 0.0f,
                    dx2 = -0.26f,
                    dy2 = -0.065f,
                    dx3 = -0.391f,
                    dy3 = -0.13f,
                )
                // c -0.195 -0.13 -0.26 -0.325 -0.195 -0.586
                curveToRelative(
                    dx1 = -0.195f,
                    dy1 = -0.13f,
                    dx2 = -0.26f,
                    dy2 = -0.325f,
                    dx3 = -0.195f,
                    dy3 = -0.586f,
                )
                // C 14.078 32.136 15.77 30.899 18.374 29.728z
                curveTo(
                    x1 = 14.078f,
                    y1 = 32.136f,
                    x2 = 15.77f,
                    y2 = 30.899f,
                    x3 = 18.374f,
                    y3 = 29.728f,
                )
                close()
            }
        }.build().also { _extPdf = it }
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
                imageVector = Icons.Rounded.ExtPdf,
                contentDescription = null,
                modifier = Modifier
                    .width((48.0).dp)
                    .height((48.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _extPdf: ImageVector? = null
