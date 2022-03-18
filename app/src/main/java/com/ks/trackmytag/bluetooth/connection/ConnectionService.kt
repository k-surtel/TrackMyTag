package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.State
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class ConnectionService(private val context: Context) {

    private val _connectionResponse = MutableLiveData<ConnectionResponse>()
    val connectionResponse: LiveData<ConnectionResponse> get() = _connectionResponse

//    private val _connectionResponse = MutableSharedFlow<ScanResults>()
//    val connectionResponse: SharedFlow<ScanResults> get() = _connectionResponse

    fun connectWithDevice(device: BluetoothDevice) {
        val connectionResponse = ConnectionResponse("", State.UNKNOWN)
        val bluetoothGatt = device.connectGatt(context, false, BluetoothGattCallback(connectionResponse))
    }
}

