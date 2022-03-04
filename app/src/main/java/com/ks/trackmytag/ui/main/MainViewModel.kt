package com.ks.trackmytag.ui.main

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.ks.trackmytag.R
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.connection.ConnectionService
import com.ks.trackmytag.bluetooth.connection.OnConnectionStateChangeListener
import com.ks.trackmytag.bluetooth.scanning.ScanResponse
import com.ks.trackmytag.data.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _savedDevices = MutableLiveData<MutableList<Device>>()
    val savedDevices: LiveData<MutableList<Device>> get() = _savedDevices
    private val connectionService = ConnectionService(context)
    private val _deviceChanged = MutableLiveData<Int>()
    val deviceChanged: LiveData<Int> get() = _deviceChanged



    private val _scanDevices = mutableListOf<BluetoothDevice>()

    private val _showScanErrorMessage = MutableLiveData<Int>()
    val showScanErrorMessage: LiveData<Int>
        get() = _showScanErrorMessage
    private val _showScanDevices = MutableLiveData<Map<String, String>>()
    val showScanDevices: LiveData<Map<String, String>>
        get() = _showScanDevices

    val scanResponse = deviceRepository.getScanResponse()



    init {
        _savedDevices.value = mutableListOf()
        setOnConnectionStateChangeListener()
    }

    private fun setOnConnectionStateChangeListener() {
        connectionService.bluetoothGattCallback.onConnectionStateChangeListener = object: OnConnectionStateChangeListener() {
            override fun onConnectionStateChange(bluetoothDevice: BluetoothDevice, state: Int) {

                val device = _savedDevices.value!!.first { it.bluetoothDevice == bluetoothDevice }

                when(state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        device.state = Device.State.CONNECTED
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        device.state = Device.State.DISCONNECTED
                    }
                }

                _deviceChanged.postValue(_savedDevices.value!!.indexOf(device))
            }
        }
    }

    fun setupBle() {
        deviceRepository.setupBle()
    }

    fun getNewDevices() {
        viewModelScope.launch {
            val scanResponse = deviceRepository.getNewDevices()
        }
    }

    fun onScanResponseReceived(scanResponse: ScanResponse) {
        _scanDevices.clear()

        if(scanResponse.errorCode != 0) {
            //0 - OK
            //1 - scan already started
            //2 - Fails to start scan as app cannot be registered.
            //3 - Fails to start scan due an internal error
            //4 - Fails to start power optimized scan as this feature is not supported.
            //5 - Fails to start scan as it is out of hardware resources.
            //6 - Fails to start scan as application tries to scan too frequently.

            _showScanErrorMessage.value = scanResponse.errorCode
        } else {
            val items = mutableMapOf<String, String>()

            scanResponse.devices.forEach { bluetoothDevice ->
                if(!savedDevices.value!!.any { it.bluetoothDevice == bluetoothDevice}){
                    _scanDevices.add(bluetoothDevice)
                    items.put(bluetoothDevice.name, bluetoothDevice.address)
                }
            }

            _showScanDevices.value = items
        }

    }

    fun saveDevice(index: Int, name: String) {
        
    }

    fun sortNewDevices(newDevices: MutableList<BluetoothDevice>?): MutableList<BluetoothDevice>? {
        newDevices?.forEach { device ->
            if(_savedDevices.value!!.any { it.bluetoothDevice == device }) newDevices.remove(device)
        }
        return newDevices
    }

//    fun formatDisplayedDeviceData(devices: MutableList<BluetoothDevice>): Array<CharSequence> {
//        var items: Array<CharSequence> = emptyArray()
//        devices.forEach { items = items.plus(context.getString(R.string.device_data, it.name, it.address)) }
//        return items
//    }

    fun addDevice(device: BluetoothDevice, name: String) {
        if(_savedDevices.value!!.none { it.address == device.address }) {
            val newDevice = Device(device, name)
            _savedDevices.postValue(_savedDevices.value!!.plus(newDevice).toMutableList())
            connectionService.connectWithDevice(newDevice)
        }
    }
}