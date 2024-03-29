package com.ks.trackmytag.bluetooth


const val GENERIC_ACCESS_SERVICE = "00001800-0000-1000-8000-00805f9b34fb"
const val DEVICE_NAME_CHARACTERISTIC = "00002a00-0000-1000-8000-00805f9b34fb" // READABLE, NOTIFIABLE
const val APPEARANCE_CHARACTERISTIC = "00002a01-0000-1000-8000-00805f9b34fb" // READABLE

const val BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb"
const val BATTERY_LEVEL_CHARACTERISTIC = "00002a19-0000-1000-8000-00805f9b34fb" // READABLE, NOTIFIABLE

const val IMMEDIATE_ALERT_SERVICE = "00001802-0000-1000-8000-00805f9b34fb"
const val ALERT_LEVEL_CHARACTERISTIC = "00002a06-0000-1000-8000-00805f9b34fb" // WRITABLE, WRITABLE WITHOUT RESPONSE, NOTIFIABLE

const val BUTTON_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
const val BUTTON_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb" // READABLE, NOTIFIABLE  button press