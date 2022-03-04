package com.ks.trackmytag.data

import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.scanning.ScanResponse

interface DeviceRepository {

    fun setupBle()

    suspend fun getNewDevices()

    fun getScanResponse(): LiveData<ScanResponse>
}