package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ConnectionService(private val context: Context) {

    private val _connectionResponse = MutableLiveData<ConnectionResponse>()
    val connectionResponse: LiveData<ConnectionResponse> get() = _connectionResponse

    fun connectWithDevice(device: BluetoothDevice) {
        val bluetoothGatt = device.connectGatt(context, false, BluetoothGattCallback(_connectionResponse))
    }
}

