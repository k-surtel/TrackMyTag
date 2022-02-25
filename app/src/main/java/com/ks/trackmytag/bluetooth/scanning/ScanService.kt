package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import android.util.Log

class ScanService(private val bluetoothManager: BluetoothManager?) {

    var scanningTime: Long = 5000

    private var scanCallback: ScanCallback = ScanCallback()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    lateinit var onScanListener: OnScanListener
    private var isScanActive = false

    fun setupBle() {
        bluetoothManager?.let {
            bluetoothAdapter = it.adapter
            bluetoothScanner = it.adapter.bluetoothLeScanner
            settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        }
    }

    fun isBleInitialized() =
        bluetoothScanner != null && settings != null && bluetoothAdapter!!.isEnabled


    fun scan() {
        Log.d("ScanService", "scanning time = " + scanningTime)
        if(isBleInitialized() && !isScanActive) {
            startScan()
            Handler(Looper.getMainLooper()).postDelayed({ stopScan() }, scanningTime)
        }
    }

    private fun startScan() {
        isScanActive = true
        onScanListener.onScanStarted()
        scanCallback.foundDevices.clear()
        bluetoothScanner?.startScan(null, settings, scanCallback)
    }

    private fun stopScan() {
        isScanActive = false
        onScanListener.onScanFinished(scanCallback.foundDevices, scanCallback.errorCode)
        bluetoothScanner?.stopScan(scanCallback)
    }
}

abstract class OnScanListener {
    abstract fun onScanStarted()
    abstract fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?)
}