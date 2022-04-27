package com.ks.trackmytag.data

import android.util.Log
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.connection.DeviceState
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.database.DevicesDao
import com.ks.trackmytag.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "TRACKTAGDeviceRepositoryImpl"

class DeviceRepositoryImpl @Inject constructor(
    private val bleManager: BleManager,
    private val devicesDao: DevicesDao,
    private val preferencesManager: PreferencesManager
    ) : DeviceRepository {

    override fun setupBle() { bleManager.setupBle() }

    override fun getDeviceStateUpdateFlow(): SharedFlow<DeviceState> {
        return bleManager.getDeviceStateUpdateFlow()
    }

    override fun getSavedDevices() = devicesDao.getSavedDevices().map {
        Log.d(TAG, "GETTING DEVICES FROM DATABASE")
        mapSavedDevices(it)
    }

    private fun mapSavedDevices(deviceList: List<Device>): List<Device> {
        Log.d(TAG, "mapSavedDevices: devices mapping, connectionState list size: ${bleManager.getConnectionStatesList().size}")
        bleManager.getConnectionStatesList().forEach { connectionState ->
            val device = deviceList.find { it.address == connectionState.address }
            device?.let {
                if(connectionState.connectionState != null) it.connectionState = connectionState.connectionState!!
            }
        }

        return deviceList
    }

    override fun getSavedDevicesAddresses(): Flow<List<String>> {
        return devicesDao.getSavedDevicesAddresses()
    }

    override fun getSavedDevicesCount(): Flow<Int> {
        return devicesDao.getSavedDevicesCount()
    }

    private suspend fun getScanTime(): Long {
        return preferencesManager.preferencesFlow.first().scanTime.toLong()
    }

    override suspend fun findNewDevices(): Flow<ScanResults> {
        return bleManager.scan(getScanTime()).map {
            filterNewDevices(it)
        }
    }

    private suspend fun filterNewDevices(scanResults: ScanResults): ScanResults {
        val savedAddresses = getSavedDevicesAddresses().first()
        scanResults.devices.forEach {
            if(savedAddresses.contains(it.address)) scanResults.devices.remove(it)
        }
        return scanResults
    }

    override suspend fun saveDevice(device: Device) {
        devicesDao.insertDevice(device)
        bleManager.connectWithDevice(device, getScanTime())
    }

    override suspend fun deleteDevice(device: Device) {
        devicesDao.deleteDevice(device)
        bleManager.disconnectDevice(device)
    }

    override suspend fun connectWithDevice(device: Device) {
        bleManager.connectWithDevice(device, getScanTime())
    }

    override suspend fun disconnectDevice(device: Device) {
        bleManager.disconnectDevice(device)
    }

    override fun deviceAlarm(device: Device) {
        bleManager.deviceAlarm(device)
    }
}