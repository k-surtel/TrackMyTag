package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothDevice

data class ScanResponse(
    val devices: MutableList<BluetoothDevice> = mutableListOf(),
    var errorCode: Int = 0
)
