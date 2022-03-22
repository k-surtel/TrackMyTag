package com.ks.trackmytag.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.ks.trackmytag.bluetooth.connection.ConnectionResponse
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.bluetooth.scanning.ScanService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "BleManager"

class BleManager @Inject constructor(@ApplicationContext context: Context) {

    private val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)

    fun setupBle() { scanService.setupBle() }

    suspend fun scan(scanTime: Long): Flow<ScanResults> = scanService.scan(scanTime)

    suspend fun connectWithDevice(device: BluetoothDevice) {
        connectionService.connectWithDevice(device)
    }

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> =
        connectionService.getConnectionResponseStateFlow()
}