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

val Icons.Rounded.ExtPowerpoint: ImageVector
    get() {
        val current = _extPowerpoint
        if (current != null) return current

        return ImageVector.Builder(
            name = "MaterialTheme.ExtPowerpoint",
            defaultWidth = 100.0.dp,
            defaultHeight = 100.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            // M8 24 c0 9.941 8.059 18 18 18 s18 -8.059 18 -18 H26 H8z
            path(
                fill = SolidColor(Color(0xFFD35230)),
            ) {
                // M 8 24
                moveTo(x = 8.0f, y = 24.0f)
                // c 0 9.941 8.059 18 18 18
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 9.941f,
                    dx2 = 8.059f,
                    dy2 = 18.0f,
                    dx3 = 18.0f,
                    dy3 = 18.0f,
                )
                // s 18 -8.059 18 -18
                reflectiveCurveToRelative(
                    dx1 = 18.0f,
                    dy1 = -8.059f,
                    dx2 = 18.0f,
                    dy2 = -18.0f,
                )
                // H 26
                horizontalLineTo(x = 26.0f)
                // H 8z
                horizontalLineTo(x = 8.0f)
                close()
            }
            // M26 6 v18 h18 C44 14.059 35.941 6 26 6z
            path(
                fill = SolidColor(Color(0xFFFF8F6B)),
            ) {
                // M 26 6
                moveTo(x = 26.0f, y = 6.0f)
                // v 18
                verticalLineToRelative(dy = 18.0f)
                // h 18
                horizontalLineToRelative(dx = 18.0f)
                // C 44 14.059 35.941 6 26 6z
                curveTo(
                    x1 = 44.0f,
                    y1 = 14.059f,
                    x2 = 35.941f,
                    y2 = 6.0f,
                    x3 = 26.0f,
                    y3 = 6.0f,
                )
                close()
            }
            // M26 6 C16.059 6 8 14.059 8 24 h18 V6z
            path(
                fill = SolidColor(Color(0xFFED6C47)),
            ) {
                // M 26 6
                moveTo(x = 26.0f, y = 6.0f)
                // C 16.059 6 8 14.059 8 24
                curveTo(
                    x1 = 16.059f,
                    y1 = 6.0f,
                    x2 = 8.0f,
                    y2 = 14.059f,
                    x3 = 8.0f,
                    y3 = 24.0f,
                )
                // h 18
                horizontalLineToRelative(dx = 18.0f)
                // V 6z
                verticalLineTo(y = 6.0f)
                close()
            }
            // M26 16.681 C26 14.648 24.352 13 22.319 13 H11.774 C9.417 16.044 8 19.852 8 24  c0 5.116 2.145 9.723 5.571 13 h8.747 C24.352 37 26 35.352 26 33.319 V16.681z
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.05f,
                strokeAlpha = 0.05f,
                strokeLineWidth = 1.0f,
            ) {
                // M 26 16.681
                moveTo(x = 26.0f, y = 16.681f)
                // C 26 14.648 24.352 13 22.319 13
                curveTo(
                    x1 = 26.0f,
                    y1 = 14.648f,
                    x2 = 24.352f,
                    y2 = 13.0f,
                    x3 = 22.319f,
                    y3 = 13.0f,
                )
                // H 11.774
                horizontalLineTo(x = 11.774f)
                // C 9.417 16.044 8 19.852 8 24
                curveTo(
                    x1 = 9.417f,
                    y1 = 16.044f,
                    x2 = 8.0f,
                    y2 = 19.852f,
                    x3 = 8.0f,
                    y3 = 24.0f,
                )
                // c 0 5.116 2.145 9.723 5.571 13
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 5.116f,
                    dx2 = 2.145f,
                    dy2 = 9.723f,
                    dx3 = 5.571f,
                    dy3 = 13.0f,
                )
                // h 8.747
                horizontalLineToRelative(dx = 8.747f)
                // C 24.352 37 26 35.352 26 33.319
                curveTo(
                    x1 = 24.352f,
                    y1 = 37.0f,
                    x2 = 26.0f,
                    y2 = 35.352f,
                    x3 = 26.0f,
                    y3 = 33.319f,
                )
                // V 16.681z
                verticalLineTo(y = 16.681f)
                close()
            }
            // M22.213 13.333 H11.525 C9.32 16.321 8 20.002 8 24 c0 4.617 1.753 8.814 4.611 12 h9.602  c1.724 0 3.121 -1.397 3.121 -3.121 V16.454 C25.333 14.731 23.936 13.333 22.213 13.333z
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.07f,
                strokeAlpha = 0.07f,
                strokeLineWidth = 1.0f,
            ) {
                // M 22.213 13.333
                moveTo(x = 22.213f, y = 13.333f)
                // H 11.525
                horizontalLineTo(x = 11.525f)
                // C 9.32 16.321 8 20.002 8 24
                curveTo(
                    x1 = 9.32f,
                    y1 = 16.321f,
                    x2 = 8.0f,
                    y2 = 20.002f,
                    x3 = 8.0f,
                    y3 = 24.0f,
                )
                // c 0 4.617 1.753 8.814 4.611 12
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 4.617f,
                    dx2 = 1.753f,
                    dy2 = 8.814f,
                    dx3 = 4.611f,
                    dy3 = 12.0f,
                )
                // h 9.602
                horizontalLineToRelative(dx = 9.602f)
                // c 1.724 0 3.121 -1.397 3.121 -3.121
                curveToRelative(
                    dx1 = 1.724f,
                    dy1 = 0.0f,
                    dx2 = 3.121f,
                    dy2 = -1.397f,
                    dx3 = 3.121f,
                    dy3 = -3.121f,
                )
                // V 16.454
                verticalLineTo(y = 16.454f)
                // C 25.333 14.731 23.936 13.333 22.213 13.333z
                curveTo(
                    x1 = 25.333f,
                    y1 = 14.731f,
                    x2 = 23.936f,
                    y2 = 13.333f,
                    x3 = 22.213f,
                    y3 = 13.333f,
                )
                close()
            }
            // M22.106 13.667 H11.276 C9.218 16.593 8 20.151 8 24 c0 4.148 1.417 7.956 3.774 11 h10.332  c1.414 0 2.56 -1.146 2.56 -2.56 V16.227 C24.667 14.813 23.52 13.667 22.106 13.667z
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.09f,
                strokeAlpha = 0.09f,
                strokeLineWidth = 1.0f,
            ) {
                // M 22.106 13.667
                moveTo(x = 22.106f, y = 13.667f)
                // H 11.276
                horizontalLineTo(x = 11.276f)
                // C 9.218 16.593 8 20.151 8 24
                curveTo(
                    x1 = 9.218f,
                    y1 = 16.593f,
                    x2 = 8.0f,
                    y2 = 20.151f,
                    x3 = 8.0f,
                    y3 = 24.0f,
                )
                // c 0 4.148 1.417 7.956 3.774 11
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 4.148f,
                    dx2 = 1.417f,
                    dy2 = 7.956f,
                    dx3 = 3.774f,
                    dy3 = 11.0f,
                )
                // h 10.332
                horizontalLineToRelative(dx = 10.332f)
                // c 1.414 0 2.56 -1.146 2.56 -2.56
                curveToRelative(
                    dx1 = 1.414f,
                    dy1 = 0.0f,
                    dx2 = 2.56f,
                    dy2 = -1.146f,
                    dx3 = 2.56f,
                    dy3 = -2.56f,
                )
                // V 16.227
                verticalLineTo(y = 16.227f)
                // C 24.667 14.813 23.52 13.667 22.106 13.667z
                curveTo(
                    x1 = 24.667f,
                    y1 = 14.813f,
                    x2 = 23.52f,
                    y2 = 13.667f,
                    x3 = 22.106f,
                    y3 = 13.667f,
                )
                close()
            }
            // M22 34 H6 c-1.105 0 -2 -0.895 -2 -2 V16 c0 -1.105 0.895 -2 2 -2 h16 c1.105 0 2 0.895 2 2 v16  C24 33.105 23.105 34 22 34z
            path(
                fill = SolidColor(Color(0xFFD35230)),
            ) {
                // M 22 34
                moveTo(x = 22.0f, y = 34.0f)
                // H 6
                horizontalLineTo(x = 6.0f)
                // c -1.105 0 -2 -0.895 -2 -2
                curveToRelative(
                    dx1 = -1.105f,
                    dy1 = 0.0f,
                    dx2 = -2.0f,
                    dy2 = -0.895f,
                    dx3 = -2.0f,
                    dy3 = -2.0f,
                )
                // V 16
                verticalLineTo(y = 16.0f)
                // c 0 -1.105 0.895 -2 2 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.105f,
                    dx2 = 0.895f,
                    dy2 = -2.0f,
                    dx3 = 2.0f,
                    dy3 = -2.0f,
                )
                // h 16
                horizontalLineToRelative(dx = 16.0f)
                // c 1.105 0 2 0.895 2 2
                curveToRelative(
                    dx1 = 1.105f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = 0.895f,
                    dx3 = 2.0f,
                    dy3 = 2.0f,
                )
                // v 16
                verticalLineToRelative(dy = 16.0f)
                // C 24 33.105 23.105 34 22 34z
                curveTo(
                    x1 = 24.0f,
                    y1 = 33.105f,
                    x2 = 23.105f,
                    y2 = 34.0f,
                    x3 = 22.0f,
                    y3 = 34.0f,
                )
                close()
            }
            // M14.673 19.012 H10 v10 h2.024 v-3.521 H14.3 c1.876 0 3.397 -1.521 3.397 -3.397 v-0.058  C17.697 20.366 16.343 19.012 14.673 19.012z M15.57 22.358 c0 0.859 -0.697 1.556 -1.556 1.556 h-1.99 v-3.325 h1.99  c0.859 0 1.556 0.697 1.556 1.556 V22.358z
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                // M 14.673 19.012
                moveTo(x = 14.673f, y = 19.012f)
                // H 10
                horizontalLineTo(x = 10.0f)
                // v 10
                verticalLineToRelative(dy = 10.0f)
                // h 2.024
                horizontalLineToRelative(dx = 2.024f)
                // v -3.521
                verticalLineToRelative(dy = -3.521f)
                // H 14.3
                horizontalLineTo(x = 14.3f)
                // c 1.876 0 3.397 -1.521 3.397 -3.397
                curveToRelative(
                    dx1 = 1.876f,
                    dy1 = 0.0f,
                    dx2 = 3.397f,
                    dy2 = -1.521f,
                    dx3 = 3.397f,
                    dy3 = -3.397f,
                )
                // v -0.058
                verticalLineToRelative(dy = -0.058f)
                // C 17.697 20.366 16.343 19.012 14.673 19.012z
                curveTo(
                    x1 = 17.697f,
                    y1 = 20.366f,
                    x2 = 16.343f,
                    y2 = 19.012f,
                    x3 = 14.673f,
                    y3 = 19.012f,
                )
                close()
                // M 15.57 22.358
                moveTo(x = 15.57f, y = 22.358f)
                // c 0 0.859 -0.697 1.556 -1.556 1.556
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.859f,
                    dx2 = -0.697f,
                    dy2 = 1.556f,
                    dx3 = -1.556f,
                    dy3 = 1.556f,
                )
                // h -1.99
                horizontalLineToRelative(dx = -1.99f)
                // v -3.325
                verticalLineToRelative(dy = -3.325f)
                // h 1.99
                horizontalLineToRelative(dx = 1.99f)
                // c 0.859 0 1.556 0.697 1.556 1.556
                curveToRelative(
                    dx1 = 0.859f,
                    dy1 = 0.0f,
                    dx2 = 1.556f,
                    dy2 = 0.697f,
                    dx3 = 1.556f,
                    dy3 = 1.556f,
                )
                // V 22.358z
                verticalLineTo(y = 22.358f)
                close()
            }
        }.build().also { _extPowerpoint = it }
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
                imageVector = Icons.Rounded.ExtPowerpoint,
                contentDescription = null,
                modifier = Modifier
                    .width((100.0).dp)
                    .height((100.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _extPowerpoint: ImageVector? = null
