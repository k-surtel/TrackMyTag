package com.ks.trackmytag.bluetooth.scanning

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.RequestManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class ScanService(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    private var isScanActive = false


    fun setupBle() {
        bluetoothScanner = bluetoothManager.adapter.bluetoothLeScanner
        settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
    }

    @SuppressLint("MissingPermission")
    suspend fun scan(scanTime: Long) = flow {
        if(!isScanActive) {
            if(checkPermissions()) {
                isScanActive = true
                Toast.makeText(context, R.string.scanning_started, Toast.LENGTH_SHORT).show()
                ScanCallback.scanResults = ScanResults()

                bluetoothScanner?.startScan(null, settings, ScanCallback)
                delay(scanTime)
                bluetoothScanner?.stopScan(ScanCallback)

                emit(ScanCallback.scanResults)

                isScanActive = false
                Toast.makeText(context, R.string.scanning_finished, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun checkPermissions(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !RequestManager.checkPermissionGranted(context, Manifest.permission.BLUETOOTH_SCAN)) {
            RequestManager.requestPermission(Manifest.permission.BLUETOOTH_SCAN)
            return false
        } else if(!RequestManager.checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            RequestManager.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            return false
        } else if(!RequestManager.isBluetoothEnabled(context)) {
            RequestManager.requestBluetoothEnabled()
            return false
        } else return true
    }
}
