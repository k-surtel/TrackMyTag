package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.*
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import com.ks.trackmytag.data.State
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

private const val TAG = "BluetoothGattCallback"

object BluetoothGattCallback : BluetoothGattCallback() {

    var connectionStateFlow = MutableStateFlow(ConnectionResponse())

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let {
            val state = when(newState) {
                BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt.discoverServices()
                    State.CONNECTED
                }
                else -> State.UNKNOWN
            }
            connectionStateFlow.value = ConnectionResponse(gatt.device.address, state)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (gatt == null) return
        with(gatt) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(services.find { it.uuid.toString() == BATTERY_SERVICE } != null)
                    readBatteryLevel(gatt)
                
                
                
                
                Log.d(TAG, "Discovered ${services.size} services for ${device.address}.")

                Log.d(TAG, "SERVICES: ${services.size}")
                for(s in services) {
                    Log.d(TAG, "CHARACTERISTICS: ${s.characteristics.size}")
                    for(c in s.characteristics) {
                        Log.d(TAG, "DESCRIPTORS: ${c.descriptors.size}")
                        for(d in c.descriptors) {
                            Log.d(TAG, "Descriptor: ${d.uuid}")
                        }
                    }
                }

            } else {
                Log.e(TAG, "Service discovery failed due to status $status")
            }
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                if (characteristic.uuid.toString() == BATTERY_LEVEL_CHARACTERISTIC) {
                    connectionStateFlow.value = connectionStateFlow.value.copy(
                        batteryLevel = characteristic.value.first().toInt()
                    )
                }
            }
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                Log.e("BluetoothGattCallback", "Read not permitted for ${characteristic.uuid}!")
            }
            else -> {
                Log.e("BluetoothGattCallback", "Characteristic read failed for ${characteristic.uuid}, error: $status")
            }
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        // this will get called anytime you perform a read or write characteristic operation
        //we’ve enabled notifications or indications on a characteristic, any incoming notification or indication
        // is delivered via BluetoothGattCallback’s onCharacteristicChanged() callback

    }

    private fun readBatteryLevel(gatt: BluetoothGatt) {
        val batteryServiceUuid = UUID.fromString(BATTERY_SERVICE)
        val batteryLevelCharUuid = UUID.fromString(BATTERY_LEVEL_CHARACTERISTIC)
        val batteryLevelChar = gatt.getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
        if (batteryLevelChar?.isReadable() == true) {
            gatt.readCharacteristic(batteryLevelChar)
        }

        //batteryLevelChar?.let { enableNotifications(gatt, it) }
    }

    fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }

        if (gatt.setCharacteristicNotification(characteristic, true)) {
            Log.d(TAG, "enableNotifications SUCCESS")
        } else {
            Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
        }
    }

    fun disableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        if (gatt.setCharacteristicNotification(characteristic, false)) {
            Log.d(TAG, "Notifications DISABLED")
        } else {
            Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
        }
    }
}











fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isIndicatable()) add("INDICATABLE")
    if (isNotifiable()) add("NOTIFIABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

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

fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0

// ... somewhere outside BluetoothGattCallback
fun ByteArray.toHexString(): String =
    joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }