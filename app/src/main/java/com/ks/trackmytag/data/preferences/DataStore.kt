package com.ks.trackmytag.data.preferences

import android.util.Log
import androidx.preference.PreferenceDataStore
import javax.inject.Inject

private const val TAG = "DataStore"

class DataStore : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {

//
//
//        when (key) {
//            "scan_time" -> preferencesManager.updateScanTime(value)
//        }

        Log.d(TAG, "putString: CALLED")
        Log.d(TAG, "putString: key($key), value($value)")

    }

    override fun getString(key: String, defValue: String?): String? {
        Log.d(TAG, "getString: CALLED")
        Log.d(TAG, "getString: key($key), defValue($defValue)")
        return when (key) {
            //TODO
            else -> defValue
        }
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