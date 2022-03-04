package com.ks.trackmytag.data

import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.bluetooth.scanning.ScanResponse

class DeviceRepositoryImpl(private val bleManager: BleManager) : DeviceRepository {

    override fun setupBle() {
        bleManager.setupBle()
    }

    override suspend fun getNewDevices() { //TODO sort through devices
        bleManager.scan()

    }

    override fun getScanResponse(): LiveData<ScanResponse> {
        return bleManager.scanResponse
    }
}