package com.example.files.view.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.files.R;

public class HomeSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.home_screen_preferences, rootKey);
    }
}
