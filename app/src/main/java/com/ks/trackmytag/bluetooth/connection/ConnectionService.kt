package com.ks.trackmytag.bluetooth.connection

import android.content.Context
import com.ks.trackmytag.bluetooth.connection.BluetoothGattCallback
import com.ks.trackmytag.data.Device

class ConnectionService(private val context: Context) {

    val bluetoothGattCallback = BluetoothGattCallback()

    fun connectWithDevice(device: Device) {
        val bluetoothGatt = device.bluetoothDevice.connectGatt(context, false, bluetoothGattCallback)
        device.status = Device.Status.CONNECTED //TODO: not here, callback!
    }
}

