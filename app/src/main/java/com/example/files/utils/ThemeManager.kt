package com.example.files.utils

import android.app.Application
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.files.R
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors

object ThemeManager {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_THEME_COLOR = "theme_color"
    private const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"

    @JvmStatic
    fun saveThemeColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_COLOR, color).apply()
        applyThemeOverlay(context, color)
    }

    @JvmStatic
    fun getThemeColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedColor = prefs.getInt(KEY_THEME_COLOR, ContextCompat.getColor(context, R.color.app_theme))
        applyThemeOverlay(context, savedColor)
        return savedColor
    }

    @JvmStatic
    fun isUsingDefaultThemeColor(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val defaultColor = ContextCompat.getColor(context, R.color.app_theme)
        return prefs.getInt(KEY_THEME_COLOR, defaultColor) == defaultColor
    }

    @JvmStatic
    fun applyThemeOverlay(context: Context, selectedColor: Int) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.AppThemeOverlay)
        contextThemeWrapper.theme.applyStyle(R.style.AppThemeOverlay, true)
    }

    @JvmStatic
    fun applyDynamicTheme(context: Context, primaryColor: Int) {
        val lighterColor = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorPrimaryContainer,
            primaryColor
        )
        val darkerColor = ColorUtils.blendARGB(primaryColor, -0x1000000, 0.2f)

        saveThemeColor(context, primaryColor)
        DynamicColors.applyToActivitiesIfAvailable(context.applicationContext as Application)
    }
}
