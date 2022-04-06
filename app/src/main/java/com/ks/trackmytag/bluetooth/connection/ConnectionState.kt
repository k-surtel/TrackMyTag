package com.ks.trackmytag.bluetooth.connection

import com.ks.trackmytag.data.State

data class ConnectionState(
    var address: String? = null,
    var state: State? = null,
    var battery: Int? = null,
    var alarm: Boolean? = null
)