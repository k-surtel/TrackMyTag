package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.scanning.ScanResponse
import com.ks.trackmytag.bluetooth.scanning.ScanService

class BleManager(context: Context) {

    private val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)

    val scanResponse = scanService.scanResponse
    val connectionResponse = connectionService.connectionResponse

    fun setupBle() {
        scanService.setupBle()
    }

    suspend fun scan() {
        return scanService.scan()
    }

    fun connectWithDevice(device: BluetoothDevice) {
        connectionService.connectWithDevice(device)
    }

}