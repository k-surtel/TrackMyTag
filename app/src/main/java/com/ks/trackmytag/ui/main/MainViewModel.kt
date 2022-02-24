package com.ks.trackmytag.ui.main

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.scanning.ScanService
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.connection.OnConnectionStateChangeListener
import com.ks.trackmytag.bluetooth.scanning.OnScanFinishedListener


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _devices = MutableLiveData<MutableList<Device>>()
    val devices: LiveData<MutableList<Device>> get() = _devices
    val scanService = ScanService(application.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
    private val connectionService = ConnectionService(application.applicationContext)
    private val _deviceChanged = MutableLiveData<Int>()
    val deviceChanged: LiveData<Int> get() = _deviceChanged

    init {
        _devices.value = mutableListOf()

        connectionService.bluetoothGattCallback.onConnectionStateChangeListener = object: OnConnectionStateChangeListener() {
            override fun onConnectionStateChange(bluetoothDevice: BluetoothDevice, state: Int) {

                val device = _devices.value!!.first { it.bluetoothDevice == bluetoothDevice }

                when(state) {
                    BluetoothProfile.STATE_CONNECTING -> Log.d("MainViewModel", "device state connecting !")
                    BluetoothProfile.STATE_DISCONNECTING -> Log.d("MainViewModel", "device state disconnecting !")
                    BluetoothProfile.STATE_CONNECTED -> {
                        device.state = Device.State.CONNECTED
                        //_devices.value!![0].state = Device.State.CONNECTED
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        device.state = Device.State.DISCONNECTED
                        //_devices.value!![0].state = Device.State.DISCONNECTED
                    }
                }

                _deviceChanged.postValue(_devices.value!!.indexOf(device))
                //_devices.postValue(_devices.value)
            }
        }
    }

    fun scan() { scanService.scan() }

    fun sortDevices(devices: MutableList<BluetoothDevice>): MutableList<BluetoothDevice> {
        devices.forEach { device ->
            if(_devices.value!!.any { it.bluetoothDevice == device }) devices.remove(device)
        }
        return devices
    }

    fun addDevice(device: BluetoothDevice, name: String) {
        if(_devices.value!!.none { it.address == device.address }) {
            val newDevice = Device(device, name)
            _devices.postValue(_devices.value!!.plus(newDevice).toMutableList())
            connectionService.connectWithDevice(newDevice)
        }
    }

    fun setOnScanFinishedListener(listener: OnScanFinishedListener) {
        scanService.onScanFinishedListener = listener
    }
}