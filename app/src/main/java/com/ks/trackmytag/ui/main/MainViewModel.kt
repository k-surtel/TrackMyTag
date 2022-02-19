package com.ks.trackmytag.ui.main

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.scanning.ScanService
import com.ks.trackmytag.bluetooth.connection.ConnectionService


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableLiveData<MutableList<Device>>()
    val devices: LiveData<MutableList<Device>> get() = _devices
    val scanService = ScanService(application.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
    private val connectionService = ConnectionService(application.applicationContext)

    init {
        _devices.value = mutableListOf()
    }

    fun scan() {
        Log.d("MainViewModel", "scan()")
        scanService.scan()
    }

    fun addDevice(device: BluetoothDevice, name: String) {
        if(_devices.value!!.none { it.address == device.address }) {
            val newDevice = Device(device, name)
            _devices.postValue(_devices.value!!.plus(newDevice).toMutableList())
            connectionService.connectWithDevice(newDevice)
        }
    }

}