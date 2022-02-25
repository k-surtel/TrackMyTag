package com.ks.trackmytag.ui.main

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.ks.trackmytag.R
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.scanning.ScanService
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.connection.OnConnectionStateChangeListener
import com.ks.trackmytag.bluetooth.scanning.OnScanListener
import kotlinx.coroutines.withContext


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

    fun sortNewDevices(newDevices: MutableList<BluetoothDevice>?): MutableList<BluetoothDevice>? {
        newDevices?.forEach { device ->
            if(_devices.value!!.any { it.bluetoothDevice == device }) newDevices.remove(device)
        }
        return newDevices
    }

    fun formatDisplayedDeviceData(devices: MutableList<BluetoothDevice>): Array<CharSequence> {
        var items: Array<CharSequence> = emptyArray()
        devices.forEach { items = items.plus(getApplication<Application>().resources.getString(R.string.device_data, it.name, it.address)) }
        return items
    }

    fun addDevice(device: BluetoothDevice, name: String) {
        if(_devices.value!!.none { it.address == device.address }) {
            val newDevice = Device(device, name)
            _devices.postValue(_devices.value!!.plus(newDevice).toMutableList())
            connectionService.connectWithDevice(newDevice)
        }
    }

    fun setOnScanListener(listener: OnScanListener) {
        scanService.onScanListener = listener
    }
}