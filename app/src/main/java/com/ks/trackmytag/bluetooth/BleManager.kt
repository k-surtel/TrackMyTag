package com.ks.trackmytag.bluetooth

import android.content.Context
import androidx.lifecycle.LiveData
import com.ks.trackmytag.bluetooth.scanning.ScanResponse
import com.ks.trackmytag.bluetooth.scanning.ScanService

class BleManager(private val context: Context) {

    private val scanService = ScanService(context)

    val scanResponse = scanService.scanResponse

    fun setupBle() {
        scanService.setupBle()
    }

    suspend fun scan() {
        return scanService.scan()
    }

}