package com.example.files

import android.app.Application
import com.example.files.utils.ThemeManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
class Files : Application() {

    override fun onCreate() {
        super.onCreate()

        val colorsOptions = DynamicColorsOptions.Builder()
            .setContentBasedSource(ThemeManager.getThemeColor(this))
            .build()
        DynamicColors.applyToActivitiesIfAvailable(this, colorsOptions)
    }
}