package com.ks.trackmytag.bluetooth.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ks.trackmytag.bluetooth.RequestManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ConnectionService"

class ConnectionService(private val context: Context) {

    private val gatts = mutableListOf<BluetoothGatt>()

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> =
        BluetoothGattCallback.connectionStateFlow.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun connectWithDevice(device: BluetoothDevice) {
        if(checkPermissions()) {
            val gatt = gatts.find { it.device == device }

            if(gatt != null) gatt.connect()
            else gatts.add(device.connectGatt(context, false, BluetoothGattCallback))
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun disconnectDevice(device: BluetoothDevice) {
        val gatt = gatts.find { it.device == device }
        if(checkPermissions()) gatt?.disconnect()
    }

    private suspend fun checkPermissions(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !RequestManager.checkPermissionGranted(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            RequestManager.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
            return false
        } else if(!RequestManager.checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            RequestManager.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            return false
        } else if(!RequestManager.isBluetoothEnabled(context)) {
            RequestManager.requestBluetoothEnabled()
            return false
        } else return true
    }
}

