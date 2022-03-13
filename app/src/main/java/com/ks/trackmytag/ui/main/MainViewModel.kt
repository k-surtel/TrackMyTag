package com.ks.trackmytag.ui.main

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val deviceRepository: DeviceRepository) : ViewModel() {

    val savedDevices = deviceRepository.getSavedDevices()
    private val _deviceChanged = MutableLiveData<Int>()
    val deviceChanged: LiveData<Int> get() = _deviceChanged



    private val _scanDevices = mutableListOf<BluetoothDevice>()

    private val _showScanErrorMessage = MutableLiveData<Int>()
    val showScanErrorMessage: LiveData<Int>
        get() = _showScanErrorMessage
    private val _showScanDevices = MutableLiveData<Map<String, String>>()
    val showScanDevices: LiveData<Map<String, String>>
        get() = _showScanDevices

    val scanResults = deviceRepository.getScanResults()
    val connectionResponse = deviceRepository.getConnectionResponse()



    fun setupBle() {
        deviceRepository.setupBle()
    }

    fun getNewDevices() {
        viewModelScope.launch {
            val scanResponse = deviceRepository.getNewDevices()
        }
    }

    fun onScanResponseReceived(scanResults: ScanResults) {
        _scanDevices.clear()

        if(scanResults.errorCode != 0) {
            //0 - OK
            //1 - scan already started
            //2 - Fails to start scan as app cannot be registered.
            //3 - Fails to start scan due an internal error
            //4 - Fails to start power optimized scan as this feature is not supported.
            //5 - Fails to start scan as it is out of hardware resources.
            //6 - Fails to start scan as application tries to scan too frequently.

            _showScanErrorMessage.value = scanResults.errorCode
        } else {
            val items = mutableMapOf<String, String>()

            scanResults.devices.forEach { bluetoothDevice ->
                if(!savedDevices.value!!.any { it.address == bluetoothDevice.address}){
                    _scanDevices.add(bluetoothDevice)
                    items.put(bluetoothDevice.name, bluetoothDevice.address)
                }
            }

            _showScanDevices.value = items
        }

    }

    fun saveDevice(index: Int, name: String) {
        viewModelScope.launch {
            val device = Device(null, name, _scanDevices[index].address)
            device.bluetoothDevice = _scanDevices[index]
            deviceRepository.saveDevice(device)
        }
    }
}