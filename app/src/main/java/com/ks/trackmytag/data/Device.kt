package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice

data class Device(
    val bluetoothDevice: BluetoothDevice,
    var name: String
) {
    val address: String = bluetoothDevice.address
    var status: Status = Status.DISCONNECTED

    enum class Status {
        DISCONNECTED,
        CONNECTED
    }
}
