package com.mgenio.jarvisofficedoor.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mgenio.jarvisofficedoor.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

//        PreferenceCategory catPref = (PreferenceCategory) findPreference(getString(R.string.location));
//        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
//        catPref.setTitle(activeLocation);
    }
}
