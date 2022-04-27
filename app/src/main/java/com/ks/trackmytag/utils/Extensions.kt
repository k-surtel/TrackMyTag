package com.ks.trackmytag.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T?>.forceUpdate(item: T) {
    this.value = null
    this.value = item
}