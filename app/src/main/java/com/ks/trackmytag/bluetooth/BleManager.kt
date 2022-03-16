package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.bluetooth.scanning.ScanService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class BleManager @Inject constructor(@ApplicationContext context: Context) {

    private val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)

    val connectionResponse = connectionService.connectionResponse

    fun setupBle() { scanService.setupBle() }

    suspend fun scan(): SharedFlow<ScanResults> {
        scanService.scan()
        return scanService.scanResultsFlow
    }

    fun connectWithDevice(device: BluetoothDevice) {
        connectionService.connectWithDevice(device)
    }

    fun getScanResultsFlow(): SharedFlow<ScanResults> {
        return scanService.scanResultsFlow
    }

}