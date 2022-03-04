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
import androidx.lifecycle.ViewModel
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.scanning.ScanService
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.connection.OnConnectionStateChangeListener
import com.ks.trackmytag.bluetooth.scanning.OnScanListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: BleManager
) : ViewModel() {

    private val _devices = MutableLiveData<MutableList<Device>>()
    val devices: LiveData<MutableList<Device>> get() = _devices
    val scanService = ScanService(context)
    private val connectionService = ConnectionService(context)
    private val _deviceChanged = MutableLiveData<Int>()
    val deviceChanged: LiveData<Int> get() = _deviceChanged

    init {
        _devices.value = mutableListOf()
        setOnConnectionStateChangeListener()
    }

    private fun setOnConnectionStateChangeListener() {
        connectionService.bluetoothGattCallback.onConnectionStateChangeListener = object: OnConnectionStateChangeListener() {
            override fun onConnectionStateChange(bluetoothDevice: BluetoothDevice, state: Int) {

                val device = _devices.value!!.first { it.bluetoothDevice == bluetoothDevice }

                when(state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        device.state = Device.State.CONNECTED
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        device.state = Device.State.DISCONNECTED
                    }
                }

                _deviceChanged.postValue(_devices.value!!.indexOf(device))
            }
        }
    }

    fun setupBle() {
        scanService.setupBle()
    }

    fun scan() {
        scanService.scan()
    }

    fun sortNewDevices(newDevices: MutableList<BluetoothDevice>?): MutableList<BluetoothDevice>? {
        newDevices?.forEach { device ->
            if(_devices.value!!.any { it.bluetoothDevice == device }) newDevices.remove(device)
        }
        return newDevices
    }

    fun formatDisplayedDeviceData(devices: MutableList<BluetoothDevice>): Array<CharSequence> {
        var items: Array<CharSequence> = emptyArray()
        devices.forEach { items = items.plus(context.getString(R.string.device_data, it.name, it.address)) }
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