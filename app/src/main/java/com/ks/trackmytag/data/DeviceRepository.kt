package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.scanning.ScanResults

interface DeviceRepository {

    fun getSavedDevices(): LiveData<List<Device>>

    fun setupBle()

    suspend fun getNewDevices()

    fun getScanResults(): LiveData<ScanResults>

    suspend fun saveDevice(device: Device)

    fun getConnectionResponse(): LiveData<Int>
}