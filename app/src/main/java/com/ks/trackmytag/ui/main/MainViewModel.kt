package com.ks.trackmytag.ui.main

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.DeviceRepository
import com.ks.trackmytag.data.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(private val deviceRepository: DeviceRepository) : ViewModel() {

    val savedDevices = deviceRepository.getSavedDevices()

    private val _connectionStates = mutableMapOf<String, State>()
    val connectionStates = ConnectionStates(_connectionStates.toMap())

    private val _scanDevices = mutableListOf<BluetoothDevice>()

    private val _showScanErrorMessage = MutableSharedFlow<Int>(0)
    val showScanErrorMessage = _showScanErrorMessage.asSharedFlow()
    private val _showScanDevices = MutableSharedFlow<Map<String, String>>()
    val showScanDevices = _showScanDevices.asSharedFlow()
    private val _deviceChanged = MutableSharedFlow<Int>()
    val deviceChanged = _deviceChanged.asSharedFlow()

    private val _connectionResponseStateFlow = deviceRepository.getConnectionResponseStateFlow()

    init { setupConnectionStateObserver() }

    private fun setupConnectionStateObserver() {
        viewModelScope.launch {
            _connectionResponseStateFlow.collectLatest { connectionResponse ->
                connectionResponse.deviceAddress?.let {
                    _connectionStates[it] = connectionResponse.newState

                    savedDevices.collectLatest { devicesList ->
                        val device = devicesList.find { it.address == connectionResponse.deviceAddress }
                        device?.let {
                            _connectionStates[it.address] = connectionResponse.newState
                            connectionStates.states = _connectionStates.toMap()
                            _deviceChanged.emit(devicesList.indexOf(it))
                        }
                    }
                }
            }
        }
    }

    fun setupBle() { deviceRepository.setupBle() }

    fun findNewDevice() {
        viewModelScope.launch {
            deviceRepository.findNewDevices().collectLatest {
                processFoundDevices(it)
            }
        }
    }

    private suspend fun processFoundDevices(scanResults: ScanResults) {
        if(scanResults.errorCode != 0) {
            //0 - OK
            //1 - scan already started
            //2 - Fails to start scan as app cannot be registered.
            //3 - Fails to start scan due an internal error
            //4 - Fails to start power optimized scan as this feature is not supported.
            //5 - Fails to start scan as it is out of hardware resources.
            //6 - Fails to start scan as application tries to scan too frequently.
            _showScanErrorMessage.emit(scanResults.errorCode)
        } else {
            _scanDevices.clear()
            val items = mutableMapOf<String, String>()

//            scanResults.devices.forEach { bluetoothDevice ->
//                if(!savedDevices.value!!.any { it.address == bluetoothDevice.address}){
//                    _scanDevices.add(bluetoothDevice)
//                    items[bluetoothDevice.name] = bluetoothDevice.address
//                }
//            }

            //temporary
            scanResults.devices.forEach { bluetoothDevice ->
                _scanDevices.add(bluetoothDevice)
                items[bluetoothDevice.name] = bluetoothDevice.address
            }

            _showScanDevices.emit(items.toMap())
        }
    }

    fun saveDevice(index: Int, name: String) {
        viewModelScope.launch {
            val device = Device(null, name, _scanDevices[index].address)
            device.bluetoothDevice = _scanDevices[index]

            deviceRepository.saveAndConnectDevice(device)
        }
    }
}