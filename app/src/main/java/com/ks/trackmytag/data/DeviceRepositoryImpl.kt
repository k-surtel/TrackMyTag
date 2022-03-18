package com.ks.trackmytag.data

import android.util.Log
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.database.DevicesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "DeviceRepositoryImpl"

class DeviceRepositoryImpl @Inject constructor(
    private val bleManager: BleManager,
    private val devicesDao: DevicesDao
    ) : DeviceRepository {

    override fun getSavedDevices() = devicesDao.getSavedDevices()

    override fun setupBle() { bleManager.setupBle() }

    override suspend fun findNewDevices(): Flow<ScanResults> {
        return bleManager.scan()
    }

    override suspend fun saveAndConnectDevice(device: Device) {
        Log.d(TAG, "saveAndConnectDevice: called")
        devicesDao.insertDevice(device)
        device.bluetoothDevice?.let {
            bleManager.connectWithDevice(it)
        }
    }

    override fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> {
        return bleManager.getConnectionResponseStateFlow()
    }
}