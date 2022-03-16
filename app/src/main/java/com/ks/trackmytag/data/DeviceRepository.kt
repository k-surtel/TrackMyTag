package com.ks.trackmytag.data

import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface DeviceRepository {

    fun getSavedDevices(): Flow<List<Device>>

    fun setupBle()

    suspend fun getNewDevices(): SharedFlow<ScanResults>

    suspend fun saveDevice(device: Device)

    fun getConnectionResponse(): LiveData<ConnectionResponse>
}