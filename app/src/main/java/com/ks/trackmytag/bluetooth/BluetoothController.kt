package com.ks.trackmytag.bluetooth

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import com.ks.trackmytag.R


fun isBleSupported(context: Context): Boolean {
    return if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle(R.string.no_ble)
        alertBuilder.setMessage(R.string.no_ble_support)
        alertBuilder.setPositiveButton(android.R.string.ok, null)
        alertBuilder.setOnDismissListener { (context as Activity).finish() }
        alertBuilder.show()
        false
    } else true
}

fun isBluetoothEnabled(context: Context) =
    (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled

