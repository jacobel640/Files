package com.example.files.utils;

import android.app.Application;
import android.content.Context;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;

public class DynamicColorUtils {
    public static void applyDynamicTheme(Context context, int primaryColor) {
        int lighterColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, primaryColor);
        int darkerColor = ColorUtils.blendARGB(primaryColor, 0xFF000000, 0.2f);

        ThemeManager.saveThemeColor(context, primaryColor);
        DynamicColors.applyToActivitiesIfAvailable((Application) context.getApplicationContext());
    }
}

