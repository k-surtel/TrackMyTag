package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import com.ks.trackmytag.data.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

private const val TAG = "TRACKTAGBluetoothGattCallback"

object BluetoothGattCallback : BluetoothGattCallback() {

    val deviceStates = mutableListOf<DeviceState>()

    private val _deviceStateUpdateFlow = MutableSharedFlow<DeviceState>()
    val deviceStateUpdateFlow = _deviceStateUpdateFlow.asSharedFlow()

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (gatt == null) return
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val state = when (newState) {
                BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices()
                    State.CONNECTED
                }
                else -> State.UNKNOWN
            }

            var deviceState = deviceStates.find { it.address == gatt.device.address }
            if (deviceState == null) {
                deviceState = DeviceState(address = gatt.device.address, connectionState = state)
                deviceStates.add(deviceState)
            } else deviceState.connectionState = state

            CoroutineScope(Dispatchers.IO).launch {
                _deviceStateUpdateFlow.emit(DeviceState(
                    address = gatt.device.address,
                    connectionState = state
                ))
            }

        } else Log.e(TAG, "Connection state change failed due to status $status")
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (gatt == null) return
        if (status == BluetoothGatt.GATT_SUCCESS) {
            readBatteryLevel(gatt)
            enableButtonPressedNotification(gatt)
            //gatt.readRemoteRssi()
        } else Log.e(TAG, "Service discovery failed due to status $status")
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                Log.d(TAG, "onCharacteristicRead: ${characteristic.uuid}")

                if(characteristic.uuid == UUID.fromString(BATTERY_LEVEL_CHARACTERISTIC)) {

                    var deviceState = deviceStates.find { it.address == gatt.device.address }
                    if (deviceState == null) {
                        deviceState = DeviceState(address = gatt.device.address, batteryLevel = characteristic.value.first().toInt())
                        deviceStates.add(deviceState)
                    } else deviceState.batteryLevel = characteristic.value.first().toInt()

                    CoroutineScope(Dispatchers.IO).launch {
                        _deviceStateUpdateFlow.emit(DeviceState(
                            address = gatt.device.address,
                            batteryLevel = characteristic.value.first().toInt()
                        ))
                    }
                }
            }

            BluetoothGatt.GATT_READ_NOT_PERMITTED ->
                Log.e("BluetoothGattCallback", "Read not permitted for ${characteristic.uuid}!")
            else ->
                Log.e("BluetoothGattCallback", "Characteristic read failed for ${characteristic.uuid}, error: $status")
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite: CALLED")
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation
        //we’ve enabled notifications or indications on a characteristic, any incoming notification or indication
        // is delivered via BluetoothGattCallback’s onCharacteristicChanged() callback
        Log.d(TAG, "onCharacteristicChanged: CALLED")
        Log.d(TAG, "char: ${characteristic?.uuid}")
        Log.d(TAG, "char value: ${characteristic?.value}")
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onReadRemoteRssi: $rssi")

            var deviceState = deviceStates.find { it.address == gatt?.device?.address }
            if (deviceState == null) {
                deviceState = DeviceState(address = gatt?.device?.address, signalStrength = abs(rssi))
                deviceStates.add(deviceState)
            } else deviceState.signalStrength = abs(rssi)

            CoroutineScope(Dispatchers.IO).launch {
                _deviceStateUpdateFlow.emit(DeviceState(
                    address = gatt?.device?.address,
                    signalStrength = abs(rssi)
                ))
            }
        }
    }

    private fun readBatteryLevel(gatt: BluetoothGatt) {
        val batteryServiceUuid = UUID.fromString(BATTERY_SERVICE)
        val batteryLevelCharUuid = UUID.fromString(BATTERY_LEVEL_CHARACTERISTIC)
        val batteryLevelChar = gatt.getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
        if (batteryLevelChar?.isReadable() == true) {
            gatt.readCharacteristic(batteryLevelChar)
        }
        batteryLevelChar?.let { enableNotifications(gatt, it) }
    }

    private fun enableButtonPressedNotification(gatt: BluetoothGatt) {
        val serviceUuid = UUID.fromString(BUTTON_SERVICE)
        val charUuid = UUID.fromString(BUTTON_CHARACTERISTIC)
        val char = gatt.getService(serviceUuid)?.getCharacteristic(charUuid)
        char?.let { enableNotifications(gatt, it) }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }

        if (gatt.setCharacteristicNotification(characteristic, true)) Log.d(TAG, "enableNotifications SUCCESS")
         else Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
    }

    private fun disableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        if (gatt.setCharacteristicNotification(characteristic, false)) Log.d(TAG, "Notifications DISABLED")
         else Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
    }
}

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0