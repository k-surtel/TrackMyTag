package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ks.trackmytag.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RequestManager {

    private var isBleSupported = false
    private val requestPermission = MutableSharedFlow<String>()
    private val requestBluetoothEnabled = MutableSharedFlow<Boolean>()

    fun isBleSupported() = isBleSupported

    fun getRequestPermissionSharedFlow(): SharedFlow<String> {
        return requestPermission.asSharedFlow()
    }

    suspend fun requestPermission(permission: String) {
        requestPermission.emit(permission)
    }

    fun getRequestBluetoothEnabledSharedFlow(): SharedFlow<Boolean> {
        return requestBluetoothEnabled.asSharedFlow()
    }

    fun isBluetoothEnabled(context: Context) =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled

    suspend fun requestBluetoothEnabled() {
        requestBluetoothEnabled.emit(true)
    }

    fun checkPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBleSupport(context: Context): Boolean {
        isBleSupported = if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.no_ble)
                .setMessage(R.string.no_ble_support)
                .setPositiveButton(android.R.string.ok, null)
                .show()

            false
        } else true
        return isBleSupported
    }
}