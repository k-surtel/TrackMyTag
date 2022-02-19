package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log

class BluetoothGattCallback : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        when(newState) {
            BluetoothProfile.STATE_CONNECTED -> Log.d("BluetoothGattCallback", "połączono")
            BluetoothProfile.STATE_DISCONNECTED -> Log.d("BluetoothGattCallback", "rozłączono od str. urządzenia")
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}