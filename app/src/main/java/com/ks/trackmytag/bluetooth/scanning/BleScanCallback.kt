package com.ks.trackmytag.bluetooth.scanning

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

class BleScanCallback(val scanResponse: ScanResponse) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.device?.let {
            if(!scanResponse.devices.contains(it)) scanResponse.devices.add(it)
        }
    }

    override fun onScanFailed(errorCode: Int) { scanResponse.errorCode = errorCode }
}