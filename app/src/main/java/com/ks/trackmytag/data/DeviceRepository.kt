package com.ks.trackmytag.data

import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DeviceRepository {

    fun getSavedDevices(): Flow<List<Device>>

    fun getSavedDevicesAddresses(): Flow<List<String>>

    fun setupBle()

    suspend fun findNewDevices(): Flow<ScanResults>

    suspend fun saveDeviceAndConnect(device: Device)

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse>
}