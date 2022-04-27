package com.ks.trackmytag.data

import com.ks.trackmytag.bluetooth.connection.DeviceState
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface DeviceRepository {

    fun setupBle()

    fun getDeviceStateUpdateFlow(): SharedFlow<DeviceState>

    fun getSavedDevices(): Flow<List<Device>>

    fun getSavedDevicesAddresses(): Flow<List<String>>

    fun getSavedDevicesCount(): Flow<Int>

    suspend fun findNewDevices(): Flow<ScanResults>

    suspend fun saveDevice(device: Device)

    suspend fun deleteDevice(device: Device)

    suspend fun connectWithDevice(device: Device)

    suspend fun disconnectDevice(device: Device)

    fun deviceAlarm(device: Device)
}