package com.ks.trackmytag.data

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.scanning.ScanResponse

interface DeviceRepository {

    fun setupBle()

    suspend fun getNewDevices()

    fun getScanResponse(): LiveData<ScanResponse>

    fun saveDevice(device: BluetoothDevice)

    fun getConnectionResponse(): LiveData<Int>
}