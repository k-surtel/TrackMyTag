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
import com.ks.trackmytag.data.DeviceStates
import com.ks.trackmytag.data.State
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
    val savedDevices = repository.getSavedDevices()
    val deviceStates = DeviceStates(mutableMapOf(), mutableMapOf(), mutableMapOf(), mutableMapOf())
    private val _connectionStateFlow = repository.getConnectionStateFlow()
    private val _deviceChanged = MutableSharedFlow<Int>()
    val deviceChanged = _deviceChanged.asSharedFlow()

    private val _selectedDeviceStateFlow = MutableStateFlow<Device?>(null)
    val selectedDeviceStateFlow = _selectedDeviceStateFlow.asStateFlow()


    init {
        setupConnectionStateObserver()
        observeSharedFlow()
    }

//    private fun mockDatabase() = viewModelScope.launch {
//        repository.getSavedDevicesCount().collectLatest { devicesCount ->
//            if (devicesCount < 3) {
//                repository.saveDevice(Device(null, "Devvv", "addr", "#d61c60"))
//                repository.saveDevice(Device(null, "Example", "addr", "#754a7d"))
//                repository.saveDevice(Device(null, "Cobytu", "addr", "#283d28"))
//                repository.saveDevice(Device(null, "Ehhhh", "addr", "#fcb103"))
//            }
//        }
//    }

    private fun observeSharedFlow() = viewModelScope.launch {
        repository.getSharedFlow().collectLatest {
            Log.d(TAG, "observeSharedFlow: state = ${it.state}")
            Log.d(TAG, "observeSharedFlow: signal = ${it.signalStrength}")
            Log.d(TAG, "observeSharedFlow: alarm = ${it.alarm}")
            Log.d(TAG, "observeSharedFlow: battery = ${it.battery}")

            
            it.state?.let { state ->
                if (it.address == selectedDeviceStateFlow.value?.address) {
                    val deviceState = selectedDeviceStateFlow.value
                    deviceState!!.connectionState = state
                    _selectedDeviceStateFlow.value = deviceState
                }
            }
        }
    }

    private fun setupConnectionStateObserver() = viewModelScope.launch {
        _connectionStateFlow.collectLatest { connectionState ->
            connectionState.state?.let {
                deviceStates.connectionStates[connectionState.address!!] = it
            }

            connectionState.signalStrength?.let {
                Log.d(TAG, "signal strenght = $it")
                deviceStates.signalStrength[connectionState.address!!] = it
            }

            connectionState.battery?.let {
                deviceStates.batteryStates[connectionState.address!!] = it
            }

            connectionState.alarm?.let {
                deviceStates.alarm[connectionState.address!!] = it
            }

            connectionState.address?.let {
                repository.getSavedDevicesAddresses().collectLatest { addresses ->
                    val changedDeviceIndex = addresses.indexOf(it)
                    if (changedDeviceIndex != -1) _deviceChanged.emit(changedDeviceIndex)
                }
            }
        }
    }

    fun chooseDevice(device: Device) {
        _selectedDeviceStateFlow.value = device
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
        val device = Device(null, name, _scanDevices[index].address, "#6e6e6e")
        device.bluetoothDevice = _scanDevices[index]
        repository.saveDevice(device)
        _scanDevices.clear()
    }

    fun deleteDevice(device: Device) = viewModelScope.launch { repository.deleteDevice(device) }

    fun onConnectionChangeClick(device: Device) {
        val connectionState = deviceStates.connectionStates[device.address]

        if (connectionState == State.CONNECTED)
            disconnectDevice(device)
        else connectWithDevice(device)
    }

    private fun connectWithDevice(device: Device) =
        viewModelScope.launch { repository.connectWithDevice(device) }

    private fun disconnectDevice(device: Device) =
        viewModelScope.launch { repository.disconnectDevice(device) }

    fun deviceAlarm(device: Device) = repository.deviceAlarm(device)

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