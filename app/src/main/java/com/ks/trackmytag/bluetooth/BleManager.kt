package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.scanning.ScanService

class BleManager(context: Context) {

    private val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)

    val scanResults = scanService.scanResults
    val connectionResponse = connectionService.connectionResponse

    fun setupBle() { scanService.setupBle() }

    suspend fun scan() { scanService.scan() }

    fun connectWithDevice(device: BluetoothDevice) {
        connectionService.connectWithDevice(device)
    }

}