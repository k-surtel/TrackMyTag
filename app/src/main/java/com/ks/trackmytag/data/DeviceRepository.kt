package com.ks.trackmytag.data

import com.ks.trackmytag.bluetooth.connection.ConnectionState
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DeviceRepository {

    fun setupBle()

    fun getConnectionStateFlow(): StateFlow<ConnectionState>

    fun getSavedDevices(): Flow<List<Device>>

    fun getSavedDevicesAddresses(): Flow<List<String>>

    suspend fun findNewDevices(): Flow<ScanResults>

    suspend fun saveDevice(device: Device)

    suspend fun deleteDevice(device: Device)

    suspend fun connectWithDevice(device: Device)

    suspend fun disconnectDevice(device: Device)

    fun deviceAlarm(device: Device)
}