package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import com.ks.trackmytag.data.Device

class BluetoothGattCallback : BluetoothGattCallback() {

    lateinit var onConnectionStateChangeListener: OnConnectionStateChangeListener

    // status - status of the connect or disconnect operation, succcess:  BluetoothGatt.GATT_SUCCESS
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let { onConnectionStateChangeListener.onConnectionStateChange(it.device, newState) }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}

abstract class OnConnectionStateChangeListener {
    abstract fun onConnectionStateChange(bluetoothDevice: BluetoothDevice, state: Int)
}