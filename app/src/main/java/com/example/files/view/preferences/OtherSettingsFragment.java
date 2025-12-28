package com.example.files.view.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.files.R;

public class OtherSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.other_settings, rootKey);
    }
}
