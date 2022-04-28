package com.ks.trackmytag.ui.adapters

import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setSignalStrengthText")
fun setSignalStrengthText(view: View, signalStrength: Int?) {
    if(signalStrength == null) (view as TextView).text = view.resources.getString(R.string.signal_strength, -1)
    else (view as TextView).text = view.resources.getString(R.string.signal_strength, signalStrength)
}

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

@BindingAdapter("setBackgroundColor")
fun setBackgroundColor(view: View, color: String?) {
    if (color != null) (view as ImageButton).background.setTint(Color.parseColor(color))
}

@BindingAdapter("setIconAlpha")
fun setIconAlpha(view: View, state: State?) {
    if (state == State.CONNECTED) (view as ImageButton).alpha = 1F
    else (view as ImageButton).alpha = 0.3F
}

@BindingAdapter("setButtonTint")
fun setButtonTint(view: View, state: State?) {
    if(state == State.CONNECTED) {
        (view as Button).setTextColor(view.resources.getColor(R.color.accent))
        view.compoundDrawables.forEach {
            it?.let { it.setTint(view.resources.getColor(R.color.accent)) }
        }
    } else {
        (view as Button).setTextColor(view.resources.getColor(R.color.text_color_dim))
        view.compoundDrawables.forEach {
            it?.let { it.setTint(view.resources.getColor(R.color.text_color_dim)) }
        }
    }
}