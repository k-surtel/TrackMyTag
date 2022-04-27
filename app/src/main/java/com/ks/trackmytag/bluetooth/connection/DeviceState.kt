package com.ks.trackmytag.bluetooth.connection

import com.ks.trackmytag.data.State

data class DeviceState(
    var address: String? = null,
    var connectionState: State? = null,
    var signalStrength: Int? = null,
    var batteryLevel: Int? = null,
    var alarm: Boolean? = null
)