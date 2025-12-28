package com.example.files;

import android.app.Application;

import com.example.files.utils.ThemeManager;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

public class Files extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        DynamicColors.applyToActivitiesIfAvailable(this);
        DynamicColorsOptions colorsOptions = new DynamicColorsOptions.Builder()
//                .setThemeOverlay(R.style.AppThemeOverlay)
                .setContentBasedSource(ThemeManager.getThemeColor(this))
                .build();
        DynamicColors.applyToActivitiesIfAvailable(this, colorsOptions);
    }
}