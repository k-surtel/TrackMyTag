package com.ks.trackmytag.ui.binding

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.ks.trackmytag.data.Device

@BindingAdapter("setButtonText")
fun setButtonText(view : View, state : Device.State) {
    (view as MaterialButton).text = if(state == Device.State.CONNECTED) "Disconnect" else "Connect"
}