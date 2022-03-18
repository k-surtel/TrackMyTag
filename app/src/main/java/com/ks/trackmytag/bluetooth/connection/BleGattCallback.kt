package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.State
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow

class BluetoothGattCallback(private val connectionResponse: ConnectionResponse) : BluetoothGattCallback() {

    // status - status of the connect or disconnect operation, succcess:  BluetoothGatt.GATT_SUCCESS
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let {
            val state = when(newState) {
                BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
                BluetoothProfile.STATE_CONNECTED -> State.CONNECTED
                else -> State.UNKNOWN
            }
            connectionResponse.deviceAddress = gatt.device.address
            connectionResponse.newState = state
        }

        //callbackFlow<> {  }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}