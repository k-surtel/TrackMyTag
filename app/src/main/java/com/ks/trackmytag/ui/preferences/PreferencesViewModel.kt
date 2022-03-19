package com.ks.trackmytag.ui.preferences

import androidx.lifecycle.*
import com.ks.trackmytag.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(val preferencesManager: PreferencesManager) : ViewModel()