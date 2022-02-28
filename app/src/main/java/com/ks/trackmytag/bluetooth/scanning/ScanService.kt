package com.ks.trackmytag.bluetooth.scanning

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.ks.trackmytag.R

class ScanService(private val context: Context) {

    var scanningTime: Long = 5000

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    private var scanCallback: ScanCallback = ScanCallback()
    lateinit var onScanListener: OnScanListener
    private var isScanActive = false

    fun setupBle() {
        bluetoothManager.let {
            bluetoothAdapter = it.adapter
            bluetoothScanner = it.adapter.bluetoothLeScanner
            settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        }
    }

    fun isBluetoothInitialized() =
        bluetoothScanner != null && settings != null && bluetoothAdapter!!.isEnabled

    fun scan() {
        if(isBluetoothInitialized() && !isScanActive) { //TODO is gps & location enabled
            startScan()
            Handler(Looper.getMainLooper()).postDelayed({ stopScan() }, scanningTime)
        }
    }

    private fun startScan() {
        isScanActive = true
        Toast.makeText(context, R.string.scanning_started, Toast.LENGTH_SHORT).show()
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
    abstract fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?)
}