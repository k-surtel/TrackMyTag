package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class ScanCallback : ScanCallback() {

    val foundDevices = mutableListOf<BluetoothDevice>()
    //TODO trzeba będzie sprawdzić czy found nie był już parowany i zapisany
    var errorCode: Int? = null

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.device?.let {
            if(!foundDevices.contains(it)) foundDevices.add(it)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.d("ScanCallback", "onScanFailed()") //TODO spr jakie tam są i obsłużyć
        this.errorCode = errorCode
    }
}