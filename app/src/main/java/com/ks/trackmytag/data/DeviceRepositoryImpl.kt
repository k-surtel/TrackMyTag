package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.database.DevicesDao
import com.ks.trackmytag.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "DeviceRepositoryImpl"

class DeviceRepositoryImpl @Inject constructor(
    private val bleManager: BleManager,
    private val devicesDao: DevicesDao,
    private val preferencesManager: PreferencesManager
    ) : DeviceRepository {

    override fun setupBle() { bleManager.setupBle() }

    override fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> {
        return bleManager.getConnectionResponseStateFlow()
    }

    override fun getSavedDevices() = devicesDao.getSavedDevices()

    override fun getSavedDevicesAddresses(): Flow<List<String>> {
        return devicesDao.getSavedDevicesAddresses()
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
}