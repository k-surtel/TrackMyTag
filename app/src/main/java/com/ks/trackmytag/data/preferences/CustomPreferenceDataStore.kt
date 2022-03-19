package com.ks.trackmytag.data.preferences

import android.util.Log
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

private const val TAG = "CustomPreferenceDataStore"

class CustomPreferenceDataStore(private val preferencesManager: PreferencesManager) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        if(!value.isNullOrEmpty()) {
            runBlocking {
                when(key) {
                    PreferencesManager.PreferencesKeys.SCAN_TIME.name -> preferencesManager.updateScanTime(value)
                }
            }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        var appPreferences: AppPreferences?
        runBlocking { appPreferences = preferencesManager.preferencesFlow.firstOrNull() }

        if(appPreferences != null) {
            return when(key) {
                PreferencesManager.PreferencesKeys.SCAN_TIME.name -> appPreferences!!.scanTime
                else -> defValue
            }
        } else return defValue
    }

    override fun putInt(key: String?, value: Int) {
        when(key) {

        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return when(key) {
            else -> defValue
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        when(key) {

        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when(key) {
            else -> defValue
        }
    }
}