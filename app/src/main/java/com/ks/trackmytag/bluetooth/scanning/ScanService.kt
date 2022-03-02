package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.isBluetoothEnabled

class ScanService(private val context: Context) {

    var scanningTime: Long = 5000

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var settings: ScanSettings? = null
    private var scanCallback: ScanCallback = ScanCallback()
    lateinit var onScanListener: OnScanListener
    private var isScanActive = false

    fun setupBle() {
        bluetoothScanner = bluetoothManager.adapter.bluetoothLeScanner
        settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
    }

    private fun isBluetoothInitialized() =
        bluetoothScanner != null && settings != null

    fun scan() {
        if(!isBluetoothEnabled(context)) requestBluetoothEnable()
        else if(isBluetoothInitialized() && !isScanActive) { //TODO is gps & location enabled
            startScan()
            Handler(Looper.getMainLooper()).postDelayed({ stopScan() }, scanningTime)
        }
    }

    private fun startScan() {
        isScanActive = true
        Toast.makeText(context, R.string.scanning_started, Toast.LENGTH_SHORT).show()
        scanCallback.foundDevices.clear()
        scanCallback.errorCode = null
        bluetoothScanner?.startScan(null, settings, scanCallback)
    }

    private fun stopScan() {
        isScanActive = false
        onScanListener.onScanFinished(scanCallback.foundDevices, scanCallback.errorCode)
        bluetoothScanner?.stopScan(scanCallback)
    }

    fun requestBluetoothEnable() {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle(R.string.enable_bluetooth)
        alertBuilder.setMessage(R.string.enable_bluetooth_question)
        alertBuilder.setNegativeButton(R.string.cancel, null)
        alertBuilder.setPositiveButton(R.string.ok) { dialog, which ->
            dialog.dismiss()
            bluetoothManager.adapter.enable()
            setupBle()
            scan()
        }
        alertBuilder.show()
    }
}

abstract class OnScanListener {
    abstract fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?)
}