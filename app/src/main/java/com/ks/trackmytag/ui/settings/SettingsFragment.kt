package com.ks.trackmytag.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ks.trackmytag.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mListPreference = preferenceManager.findPreference<Preference>("scan_time") as ListPreference?
        mListPreference?.setOnPreferenceChangeListener { preference, newValue ->

            true
        }

    }
}