package com.ks.trackmytag.ui.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ks.trackmytag.R
import com.ks.trackmytag.data.preferences.DataStore

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStore()
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}