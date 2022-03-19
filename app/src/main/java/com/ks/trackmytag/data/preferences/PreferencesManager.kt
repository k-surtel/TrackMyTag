package com.ks.trackmytag.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesManager"

data class AppPreferences(val scanTime: String)

class PreferencesManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    object PreferencesKeys {
        val SCAN_TIME = stringPreferencesKey("scan_time")
    }

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                Log.e(TAG, ": Error reading preferences", exception)
                emit(emptyPreferences())
            } else throw exception
        }
        .map { preferences ->
        val scanTime = preferences[PreferencesKeys.SCAN_TIME] ?: "5000"    // TODO - 5 sec or 5000 milisec??

        AppPreferences(scanTime)
    }

    suspend fun updateScanTime(scanTime: String) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.SCAN_TIME] = scanTime
        }
    }
}