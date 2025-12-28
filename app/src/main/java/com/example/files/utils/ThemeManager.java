package com.example.files.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ContextThemeWrapper;

import androidx.core.content.ContextCompat;

import com.example.files.R;

public class ThemeManager {
    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_THEME_COLOR = "theme_color";

    public static void saveThemeColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME_COLOR, color);
        editor.apply();
        applyThemeOverlay(context, color);
    }

    public static int getThemeColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int savedColor = prefs.getInt(KEY_THEME_COLOR, ContextCompat.getColor(context, R.color.primary));
        applyThemeOverlay(context, savedColor);
        return  savedColor;
    }

    public static void applyThemeOverlay(Context context, int selectedColor) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.AppThemeOverlay);
        contextThemeWrapper.getTheme().applyStyle(R.style.AppThemeOverlay, true);
    }
}

