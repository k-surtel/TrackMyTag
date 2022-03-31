package com.ks.trackmytag.ui.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setBatteryLevelText")
fun setBatteryLevelText(view: View, batteryLevel: Int?) {
    if(batteryLevel == null) (view as TextView).text = view.resources.getString(R.string.battery_level, -1)
    else (view as TextView).text = view.resources.getString(R.string.battery_level, batteryLevel)
}

@BindingAdapter("setStateText")
fun setStateText(view: View, state: State?) {
    if(state == State.CONNECTED) (view as TextView).text = view.resources.getString(R.string.connected)
    else (view as TextView).text = view.resources.getString(R.string.disconnected)
}

@BindingAdapter("setVisibleWhenConnected")
fun setVisibleWhenConnected(view: View, state: State?) {
    if(state == State.CONNECTED) view.visibility = View.VISIBLE
    else view.visibility = View.INVISIBLE
}

@BindingAdapter("setVisibleWhenDisconnected")
fun setVisibleWhenDisconnected(view: View, state: State?) {
    if(state == State.CONNECTED) view.visibility = View.INVISIBLE
    else view.visibility = View.VISIBLE
}