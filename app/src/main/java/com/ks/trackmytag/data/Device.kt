package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice

data class Device(
    val bluetoothDevice: BluetoothDevice,
    var name: String
) {
    val address: String = bluetoothDevice.address
    var state: State = State.DISCONNECTED

    enum class State {
        DISCONNECTED,
        CONNECTED
    }
}
