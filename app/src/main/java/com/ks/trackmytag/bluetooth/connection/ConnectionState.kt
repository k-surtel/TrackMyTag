package com.ks.trackmytag.bluetooth.connection

import com.ks.trackmytag.data.State

data class ConnectionState(
    var deviceAddress: String? = null,
    var newState: State = State.UNKNOWN,
    var batteryLevel: Int = -1,
    var alarm: Boolean = false
)