package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.database.DevicesDao

class DeviceRepositoryImpl(
    private val bleManager: BleManager,
    private val devicesDao: DevicesDao
    ) : DeviceRepository {

    override fun getSavedDevices() = devicesDao.getSavedDevices()

    override fun setupBle() { bleManager.setupBle() }

    override suspend fun getNewDevices() { bleManager.scan() }

    override fun getScanResults(): LiveData<ScanResults> {
        return bleManager.scanResults
    }

    override suspend fun saveDevice(device: Device) {
        devicesDao.insertDevice(device)
        device.bluetoothDevice?.let { bleManager.connectWithDevice(it) }
    }

    override fun getConnectionResponse(): LiveData<Int> {
        return bleManager.connectionResponse
    }
}