package com.ks.trackmytag.bluetooth.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Build
import com.ks.trackmytag.bluetooth.ALERT_LEVEL_CHARACTERISTIC
import com.ks.trackmytag.bluetooth.IMMEDIATE_ALERT_SERVICE
import com.ks.trackmytag.bluetooth.RequestManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

private const val TAG = "TRACKTAGConnectionService"

class ConnectionService(private val context: Context) {

    private val gatts = mutableMapOf<String, BluetoothGatt>()
    private val alarms = mutableMapOf<String, Boolean>()
    private val coroutines = mutableMapOf<String, Job>()

    fun getDeviceStateUpdateFlow(): SharedFlow<DeviceState> = BluetoothGattCallback.deviceStateUpdateFlow

    @SuppressLint("MissingPermission")
    suspend fun connectWithDevice(device: BluetoothDevice) {
        if(hasPermissions()) {
            var gatt = gatts[device.address]

            if(gatt != null) gatt.connect()
            else {
                gatt = device.connectGatt(context, false, BluetoothGattCallback)
                gatts[device.address] = gatt
            }

            gatt?.let {
                //coroutines.put(device.address, setConnectionSignalChecking(it))
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun disconnectDevice(device: BluetoothDevice) {
        val gatt = gatts[device.address]
        if(hasPermissions()) {
            gatt?.disconnect()
            //gatt?.close()
            //gatts.remove(device.address)

            coroutines[device.address]?.cancelAndJoin()
            coroutines.remove(device.address)
        }
    }

    fun alarm(address: String) {
        val gatt = gatts[address]
        gatt?.let { gatt ->
            val service = gatt.getService(UUID.fromString(IMMEDIATE_ALERT_SERVICE))
            val characteristic = service?.getCharacteristic(UUID.fromString(
                ALERT_LEVEL_CHARACTERISTIC
            ))

            characteristic?.let {
                if (alarms[address] == null) alarms[address] = false

                if(alarms[address]!!) {
                    alarms[address] = false
                    writeCharacteristic(gatt, it, byteArrayOf(0x00))
                } else {
                    alarms[address] = true
                    writeCharacteristic(gatt, it, byteArrayOf(0x01))
                }
            }
        }
    }

    private suspend fun setConnectionSignalChecking(gatt: BluetoothGatt): Job = GlobalScope.launch {
        while (true) {
            // TODO check if still connected (w/ bt manager)

            gatt.readRemoteRssi()
            delay(10000)
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

    private fun writeCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        characteristic.writeType = writeType
        characteristic.value = payload
        gatt.writeCharacteristic(characteristic)
    }
}

