package com.ks.trackmytag.bluetooth.connection

import com.ks.trackmytag.data.State

data class ConnectionResponse(
    var deviceAddress: String,
    var newState: State
)