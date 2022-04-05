package com.ks.trackmytag.bluetooth.connection

import com.ks.trackmytag.data.State

data class ConnectionState(
    var deviceAddress: String? = null,
    var newState: State? = null,
    var batteryLevel: Int? = null,
    var alarm: Boolean? = null
)