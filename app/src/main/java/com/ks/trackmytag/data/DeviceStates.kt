package com.ks.trackmytag.data

data class DeviceStates(
    var connectionStates: MutableMap<String, State>,
    var batteryStates: MutableMap<String, Int>,
    var alarm: MutableMap<String, Boolean>
)