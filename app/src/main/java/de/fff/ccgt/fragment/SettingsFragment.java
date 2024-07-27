package de.fff.ccgt.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import de.fff.ccgt.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}