package com.ks.trackmytag.ui.binding

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setStateText")
fun setStateText(view : View, state : State?) {
    if(state == null) (view as TextView).text = State.DISCONNECTED.name
    else (view as TextView).text = state.name
}

@BindingAdapter("setButtonText")
fun setButtonText(view : View, state : State) {
    (view as MaterialButton).text = if(state == State.CONNECTED) view.resources.getString(R.string.disconnect)
    else view.resources.getString(R.string.connect)
}