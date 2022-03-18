package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import com.ks.trackmytag.data.State
import kotlinx.coroutines.flow.MutableStateFlow

object BluetoothGattCallback : BluetoothGattCallback() {

    var connectionStateFlow = MutableStateFlow(ConnectionResponse())

    // status - status of the connect or disconnect operation, succcess:  BluetoothGatt.GATT_SUCCESS
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Log.d("MainView for log", "onConnectionStateChange: called")
        gatt?.let {
            val state = when(newState) {
                BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
                BluetoothProfile.STATE_CONNECTED -> State.CONNECTED
                else -> State.UNKNOWN
            }

            connectionStateFlow.value = ConnectionResponse(gatt.device.address, state)

//            connectionStateFlow.value.deviceAddress = gatt.device.address
//            connectionStateFlow.value.newState = state


            //connectionResponse = ConnectionResponse(gatt.device.address, state)
//            connectionResponse.deviceAddress = gatt.device.address
//            connectionResponse.newState = state
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation

    }
}