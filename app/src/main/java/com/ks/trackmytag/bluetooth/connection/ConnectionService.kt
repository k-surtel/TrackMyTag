package com.ks.trackmytag.bluetooth.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.util.Log
import com.ks.trackmytag.bluetooth.RequestManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

private const val TAG = "ConnectionService"

class ConnectionService(private val context: Context) {

    private val gatts = mutableMapOf<String, BluetoothGatt>()

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> =
        BluetoothGattCallback.connectionStateFlow.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun connectWithDevice(device: BluetoothDevice) {
        if(hasPermissions()) {
            var gatt = gatts[device.address]

            if(gatt != null) gatt.connect()
            else {
                gatt = device.connectGatt(context, false, BluetoothGattCallback)
                gatts.put(device.address, gatt)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun disconnectDevice(device: BluetoothDevice) {
        val gatt = gatts[device.address]
        if(hasPermissions()) {
            gatt?.disconnect()
            gatt?.close()
            gatts.remove(device.address)
        }
    }

    private suspend fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !RequestManager.checkPermissionGranted(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            RequestManager.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
            return false
        } else if (!RequestManager.checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            RequestManager.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            return false
        } else if (!RequestManager.isBluetoothEnabled(context)) {
            RequestManager.requestBluetoothEnabled()
            return false
        } else return true
    }






    fun writeCharacteristic(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        gatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }
}

