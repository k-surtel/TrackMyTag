package com.ks.trackmytag.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ConnectionService"

class ConnectionService(private val context: Context) {

    fun getConnectionResponseStateFlow(): StateFlow<ConnectionResponse> =
        BluetoothGattCallback.connectionStateFlow.asStateFlow()

//    @SuppressLint("MissingPermission") //TODO
//    suspend fun connectWithDevice3(device: BluetoothDevice): ConnectionResponse {
//        Log.d(TAG, "connectWithDevice3: called")
//        return suspendCoroutine { continuation ->
//
//            val connectionResponse = ConnectionResponse("", State.UNKNOWN)
//
//            val bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
//
//                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//                    Log.d(TAG, "onConnectionStateChange: called; newState = $newState")
//                    gatt?.let {
//                        val state = when (newState) {
//                            BluetoothProfile.STATE_DISCONNECTED -> State.DISCONNECTED
//                            BluetoothProfile.STATE_CONNECTED -> State.CONNECTED
//                            else -> State.UNKNOWN
//                        }
//                        connectionResponse.deviceAddress = gatt.device.address
//                        connectionResponse.newState = state
//                    }
//
//                    continuation.resume(connectionResponse) // unhandled exception already resumed...
//                }
//            })
//        }
//    }

//        fun connectWithDevice2(device: BluetoothDevice) {
//            val connectionResponse = ConnectionResponse("", State.UNKNOWN)
//            val bluetoothGatt = device.connectGatt(context, false, BluetoothGattCallback)
//        }
//
        fun connectWithDevice(device: BluetoothDevice) {
            val bluetoothGatt = device.connectGatt(context, false, BluetoothGattCallback)
        }
    }

