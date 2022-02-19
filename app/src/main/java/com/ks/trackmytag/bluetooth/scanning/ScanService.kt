package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper

class ScanService(private val bluetoothManager: BluetoothManager?) {

    var scanningTime: Long = 5000

    private var scanCallback: ScanCallback = ScanCallback()
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    lateinit var onScanFinishedListener: OnScanFinishedListener

    fun setupBle() {
        bluetoothManager?.let {
            bluetoothScanner = it.adapter.bluetoothLeScanner
            settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        }
    }

    fun isBleInitialized() =
        bluetoothScanner != null && settings != null


    fun scan() {
        if(isBleInitialized()) {
            startScan()
            Handler(Looper.getMainLooper()).postDelayed({ stopScan() }, scanningTime)
        }
    }

    private fun startScan() {
        scanCallback.foundDevices.clear()
        bluetoothScanner?.startScan(null, settings, scanCallback)
    }

    private fun stopScan() {
        bluetoothScanner?.stopScan(scanCallback)
        onScanFinishedListener.onScanFinished(scanCallback.foundDevices, scanCallback.errorCode)
    }
}

class OnScanFinishedListener(val onScanFinishedListener: (devices: MutableList<BluetoothDevice>?, errorCode: Int?) -> Unit) {
    fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?) = onScanFinishedListener(devices, errorCode)
}