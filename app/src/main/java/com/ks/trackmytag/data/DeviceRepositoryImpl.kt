package com.ks.trackmytag.data

import android.util.Log
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.database.DevicesDao
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "DeviceRepositoryImpl"

class DeviceRepositoryImpl @Inject constructor(
    private val bleManager: BleManager,
    private val devicesDao: DevicesDao
    ) : DeviceRepository {

    override fun getSavedDevices() = devicesDao.getSavedDevices()

    override fun getSavedDevicesAddresses(): Flow<List<String>> {
        return devicesDao.getSavedDevicesAddresses()
    }

    override fun setupBle() { bleManager.setupBle() }

    override suspend fun findNewDevices(): Flow<ScanResults> {
        return bleManager.scan().map {
            filterNewDevices(it)
        }
    }

    private suspend fun filterNewDevices(scanResults: ScanResults): ScanResults {
        val savedAddresses = getSavedDevicesAddresses().firstOrNull()
        savedAddresses?.let {
            scanResults.devices.forEach {
                if(savedAddresses.contains(it.address)) scanResults.devices.remove(it)
            }
        }
        return scanResults
    }

    override suspend fun saveDeviceAndConnect(device: Device) {
        devicesDao.insertDevice(device)
        device.bluetoothDevice?.let {
            bleManager.connectWithDevice(it)
        }
    }

    override fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> {
        return bleManager.getConnectionResponseStateFlow()
    }
}