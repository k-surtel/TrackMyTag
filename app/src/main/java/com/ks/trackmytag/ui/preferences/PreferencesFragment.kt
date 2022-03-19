package com.ks.trackmytag.ui.preferences

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import com.ks.trackmytag.R
import com.ks.trackmytag.data.preferences.CustomPreferenceDataStore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat() {

    private val viewModel: PreferencesViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = CustomPreferenceDataStore(viewModel.preferencesManager)
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}