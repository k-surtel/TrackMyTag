package com.ks.trackmytag.bluetooth.connection

import android.content.Context
import com.ks.trackmytag.data.Device

class ConnectionService(private val context: Context) {

    val bluetoothGattCallback = BluetoothGattCallback()

    fun connectWithDevice(device: Device) {
        val bluetoothGatt = device.bluetoothDevice.connectGatt(context, false, bluetoothGattCallback)
    }
}

