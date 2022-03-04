package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothDevice

data class ScanResponse(
    val foundDevices: MutableList<BluetoothDevice> = mutableListOf(),
    var errorCode: Int = 0
)
