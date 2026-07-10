package com.example.files.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.files.R;
import com.example.files.utils.ThemeManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        int primaryColor = ThemeManager.getThemeColor(this);
////        DynamicColorUtils.applyDynamicTheme(this, primaryColor);
//        Resources.Theme theme = getResources().newTheme();
//        theme.applyStyle(R.style.AppThemeOverlay, true); // החלה של ערכת נושא זמנית
//
//        // שינוי הערכים הדינמיים (primaryColor)
//        theme.resolveAttribute(R.attr.dynamicColorPrimary, new TypedValue(), true);
//
//        // עדכון הצבע על כל האפליקציה
//        getWindow().setStatusBarColor(primaryColor);
//
//        applyTheme(primaryColor);

        super.onCreate(savedInstanceState, persistentState);
    }

    public void applyTheme(int primaryColor) {
        // יצירת נושא חדש שיכלול את הצבעים שהמשתמש בחר
        Resources.Theme newTheme = getResources().newTheme();
        newTheme.applyStyle(R.style.AppThemeOverlay, true);

        // כאן תוכל להחיל את הצבעים החדשים על הנושא, למשל:
        newTheme.resolveAttribute(R.attr.dynamicColorPrimary, new TypedValue(), true);
        newTheme.resolveAttribute(R.attr.dynamicColorPrimaryContainer, new TypedValue(), true);

        // החלת הנושא החדש
        setTheme(R.style.Theme_Files);
        // אחרי החלת הנושא מחדש, כל רכיבי UI יקבלו את הצבעים החדשים
    }

}
