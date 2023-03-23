package com.ks.trackmytag.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.ks.trackmytag.R
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.State
import kotlinx.android.synthetic.main.dialog_settings.view.*

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
    if (color != null) {

        val color = Color.parseColor(color)

        if (view is ImageView) {
            val drawable: GradientDrawable = view.background as GradientDrawable
            drawable.mutate()
            drawable.setStroke(5, color)
        } else if (view is Button) {
            view.background.setTint(color)
            if (ColorUtils.calculateLuminance(color) < 0.5) view.setTextColor(Color.WHITE)
            else view.setTextColor(Color.BLACK)
        }
    }
}

@BindingAdapter("setIconAlpha")
fun setIconAlpha(view: View, state: State?) {
    if (state == State.CONNECTED) (view as Button).alpha = 1F
    else (view as Button).alpha = 0.3F
}

@BindingAdapter("setIconSize")
fun setIconSize(view: View, state: State?) {
    val params: ViewGroup.LayoutParams = view.layoutParams

    when (state) {
        State.CONNECTED -> {
            if (view is Button) {
                params.width = 180
                params.height = 180
            } else if (view is ImageView) {
                params.width = 210
                params.height = 210
            }
        }
        else -> {
            if (view is Button) {
                params.width = 150
                params.height = 150
            } else if (view is ImageView) {
                params.width = 180
                params.height = 180
            }
        }
    }

    view.layoutParams = params
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

@BindingAdapter("alarmEnabled")
fun alarmEnabled(view: View, state: State?) {
    (view as Button).isEnabled = state == State.CONNECTED
}

@BindingAdapter(value = ["device", "selected"], requireAll = false)
fun setSelectedIndicationVisibility(view: ImageView, device: Device, selected: Boolean) {
    if (device.selected) view.visibility = View.VISIBLE
    else view.visibility = View.INVISIBLE

    if (selected) view.visibility = View.VISIBLE
}