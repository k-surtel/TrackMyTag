package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ks.trackmytag.data.Device

class BluetoothGattCallback(private val _connectionResponse: MutableLiveData<Int>) : BluetoothGattCallback() {

    // status - status of the connect or disconnect operation, succcess:  BluetoothGatt.GATT_SUCCESS
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let {
            _connectionResponse.postValue(newState)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}