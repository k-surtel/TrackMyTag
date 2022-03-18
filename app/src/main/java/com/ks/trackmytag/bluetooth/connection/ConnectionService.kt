package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ConnectionService"

class ConnectionService(private val context: Context) {

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> =
        BluetoothGattCallback.connectionStateFlow.asStateFlow()

        fun connectWithDevice(device: BluetoothDevice) {
            val bluetoothGatt = device.connectGatt(context, false, BluetoothGattCallback)
        }
    }

