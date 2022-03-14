package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.State

class BluetoothGattCallback(private val _connectionResponse: MutableLiveData<ConnectionResponse>) : BluetoothGattCallback() {

    // status - status of the connect or disconnect operation, succcess:  BluetoothGatt.GATT_SUCCESS
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let {
            val state = when(newState) {
                BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
                BluetoothProfile.STATE_CONNECTED -> State.CONNECTED
                else -> State.UNKNOWN
            }
            _connectionResponse.postValue(ConnectionResponse(gatt.device.address, state))
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}