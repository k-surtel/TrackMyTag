package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.widget.Toast
import com.ks.trackmytag.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*


class ScanService(private val context: Context) {

    private var scanTime = 5000L

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    private var isScanActive = false

    private val _scanResultsFlow = MutableSharedFlow<ScanResults>()
    val scanResultsFlow: SharedFlow<ScanResults> get() = _scanResultsFlow


    fun setupBle() {
        bluetoothScanner = bluetoothManager.adapter.bluetoothLeScanner
        settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
    }

    fun setScanTime(time: Long) {
        scanTime = time
    }

    suspend fun scan() = flow {
        if(!isScanActive) {
            ScanCallback.prepareForScan()

            isScanActive = true
            Toast.makeText(context, R.string.scanning_started, Toast.LENGTH_SHORT).show()

            bluetoothScanner?.startScan(null, settings, ScanCallback)
            delay(scanTime)
            bluetoothScanner?.stopScan(ScanCallback)

            emit(ScanCallback.scanResults)

            isScanActive = false
            Toast.makeText(context, R.string.scanning_finished, Toast.LENGTH_SHORT).show()
        }
    }

}
