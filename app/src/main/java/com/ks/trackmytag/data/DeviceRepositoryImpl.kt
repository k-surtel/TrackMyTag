package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.scanning.ScanResponse

class DeviceRepositoryImpl(
    private val bleManager: BleManager
    ) : DeviceRepository {

    override fun setupBle() { bleManager.setupBle() }

    override suspend fun getNewDevices() { //TODO sort through devices
        bleManager.scan()
    }

    override fun getScanResponse(): LiveData<ScanResponse> {
        return bleManager.scanResponse
    }

    override fun saveDevice(device: BluetoothDevice) {
        //TODO save device
        bleManager.connectWithDevice(device)
    }

    override fun getConnectionResponse(): LiveData<Int> {
        return bleManager.connectionResponse
    }
}