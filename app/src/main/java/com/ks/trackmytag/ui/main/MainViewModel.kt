package com.ks.trackmytag.ui.main

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.ks.trackmytag.bluetooth.RequestManager
import com.ks.trackmytag.bluetooth.scanning.ScanResults
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.DeviceRepository
import com.ks.trackmytag.data.State
import com.ks.trackmytag.ui.adapters.DeviceIconAdapter
import com.ks.trackmytag.ui.adapters.DeviceIconClickListener
import com.ks.trackmytag.utils.forceUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TRACKTAGMainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: DeviceRepository) : ViewModel() {

    // Permission & BT handling
    val requestPermission = RequestManager.getRequestPermissionSharedFlow()
    val requestBluetoothEnabled = RequestManager.getRequestBluetoothEnabledSharedFlow()

    // Scanning
    private val _scanDevices = mutableListOf<BluetoothDevice>()
    private val _showScanErrorMessage = MutableSharedFlow<Int>(0)
    val showScanErrorMessage = _showScanErrorMessage.asSharedFlow()
    private val _showScanDevices = MutableSharedFlow<Map<String, String>>()
    val showScanDevices = _showScanDevices.asSharedFlow()

    // Saved devices
    private val savedDevices = repository.getSavedDevices()
    private val _selectedDeviceStateFlow = MutableStateFlow<Device?>(null)
    val selectedDeviceStateFlow = _selectedDeviceStateFlow.asStateFlow()

    val adapter = DeviceIconAdapter(DeviceIconClickListener {
        _selectedDeviceStateFlow.value = it
    })

    init {
        observeDeviceStates()

        viewModelScope.launch {
            savedDevices.collectLatest {
                adapter.submitList(it)
                if (_selectedDeviceStateFlow.value == null && it.isNotEmpty())
                    _selectedDeviceStateFlow.value = it[0]
            }
        }
    }

    private fun observeDeviceStates() = viewModelScope.launch {
        repository.getDeviceStateUpdateFlow().collectLatest { deviceState ->
            if (deviceState.address != null) {
                val device = adapter.currentList.find { it.address == deviceState.address }
                if (device != null) {

                    deviceState.connectionState?.let { device.connectionState = it }

                    deviceState.signalStrength?.let { device.signalStrength = it }

                    deviceState.batteryLevel?.let { device.batteryLevel = it }

                    deviceState.alarm?.let {
                        //TODO
                    }

                    if(_selectedDeviceStateFlow.value?.address == deviceState.address)
                        _selectedDeviceStateFlow.forceUpdate(device)

                    adapter.notifyItemChanged(adapter.currentList.indexOf(device))
                }
            }
        }
    }

    fun setupBle() = repository.setupBle()

    fun findDevices() = viewModelScope.launch {
        repository.findNewDevices().collectLatest {
            processFoundDevices(it)
        }
    }

    private suspend fun processFoundDevices(scanResults: ScanResults) {
        if (scanResults.errorCode != 0) {
            //0 - OK
            //1 - scan already started
            //2 - Fails to start scan as app cannot be registered.
            //3 - Fails to start scan due an internal error
            //4 - Fails to start power optimized scan as this feature is not supported.
            //5 - Fails to start scan as it is out of hardware resources.
            //6 - Fails to start scan as application tries to scan too frequently.
            _showScanErrorMessage.emit(scanResults.errorCode)
        } else {
            val items = mutableMapOf<String, String>()
            scanResults.devices.forEach { bluetoothDevice ->
                _scanDevices.add(bluetoothDevice)
                items[bluetoothDevice.name] = bluetoothDevice.address
            }

            _showScanDevices.emit(items.toMap())
        }
    }

    fun saveDevice(index: Int, name: String) = viewModelScope.launch {
        val device = Device(null, name, _scanDevices[index].address)
        device.bluetoothDevice = _scanDevices[index]
        repository.saveDevice(device)
        _scanDevices.clear()
    }

    fun deleteDevice() = viewModelScope.launch {
        selectedDeviceStateFlow.value?.let {
            repository.deleteDevice(it)
            // TODO change selected device
        }
    }

    fun updateDevice(name: String, color: String) = viewModelScope.launch {
        selectedDeviceStateFlow.value?.let {
            if (it.name != name || it.color != color) {
                it.name = name
                it.color = color
                repository.updateDevice(it)
                _selectedDeviceStateFlow.forceUpdate(it)
                adapter.notifyItemChanged(adapter.currentList.indexOf(it))
            }
        }
    }

    fun onConnectionChangeClick() {
        if (selectedDeviceStateFlow.value == null) return
        if (selectedDeviceStateFlow.value!!.connectionState == State.CONNECTED)
            disconnectDevice(selectedDeviceStateFlow.value!!)
        else connectWithDevice(selectedDeviceStateFlow.value!!)
    }

    private fun connectWithDevice(device: Device) =
        viewModelScope.launch { repository.connectWithDevice(device) }

    private fun disconnectDevice(device: Device) =
        viewModelScope.launch { repository.disconnectDevice(device) }

    fun deviceAlarm() = selectedDeviceStateFlow.value?.let { repository.deviceAlarm(it) }

    fun handlePermissionsAndBluetooth(context: Context) {
        if (!RequestManager.checkPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION))
            viewModelScope.launch { RequestManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!RequestManager.checkPermissionGranted(context, Manifest.permission.BLUETOOTH_SCAN))
                viewModelScope.launch { RequestManager.requestPermission(Manifest.permission.BLUETOOTH_SCAN) }

            if (!RequestManager.checkPermissionGranted(context, Manifest.permission.BLUETOOTH_CONNECT))
                viewModelScope.launch { RequestManager.requestPermission(Manifest.permission.BLUETOOTH_CONNECT) }
        }

        viewModelScope.launch { RequestManager.requestBluetoothEnabled() }
    }
}