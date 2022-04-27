package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.ks.trackmytag.bluetooth.connection.BluetoothGattCallback
import com.ks.trackmytag.bluetooth.connection.DeviceState
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.bluetooth.scanning.ScanService
import com.ks.trackmytag.data.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

private const val TAG = "TRACKTAGBleManager"

class BleManager @Inject constructor(@ApplicationContext context: Context) {

    private val devices = mutableListOf<BluetoothDevice>()

    private val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)

    fun getConnectionStatesList(): List<DeviceState> {
        return BluetoothGattCallback.deviceStates.toList()
    }

    fun setupBle() {
        scanService.setupBle()

    }

    suspend fun scan(scanTime: Long): Flow<ScanResults> = scanService.scan(scanTime)

    private suspend fun searchForDevice(address: String, scanTime: Long): BluetoothDevice? {
        var device: BluetoothDevice? = null
        scanService.searchForDevice(address, scanTime).collectLatest {
            device = it
        }
        return device
    }

    fun getDeviceStateUpdateFlow(): SharedFlow<DeviceState> =
        connectionService.getDeviceStateUpdateFlow()

    suspend fun connectWithDevice(device: Device, scanTime: Long) {
        var bluetoothDevice = device.bluetoothDevice

        if(bluetoothDevice == null)
            bluetoothDevice = devices.find { it.address == device.address }

        if(bluetoothDevice == null)
            bluetoothDevice = searchForDevice(device.address, scanTime)

        bluetoothDevice?.let {
            devices.add(it)
            connectionService.connectWithDevice(bluetoothDevice)
        }
    }

    suspend fun disconnectDevice(device: Device) {
        val bluetoothDevice = devices.find { it.address == device.address }
        bluetoothDevice?.let { connectionService.disconnectDevice(it) }
    }

    fun deviceAlarm(device: Device) {
        connectionService.alarm(device.address)
    }
}