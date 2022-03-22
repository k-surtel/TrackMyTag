package com.ks.trackmytag.ui.adapters

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setStateText")
fun setStateText(view : View, state : State?) {
    (view as TextView).text = state?.name ?: State.DISCONNECTED.name
}

@BindingAdapter("setButtonText")
fun setButtonText(view : View, state : State?) {
    if(state == null) (view as MaterialButton).text = view.resources.getString(R.string.connect)
    else {
            (view as MaterialButton).text =
                if(state == State.CONNECTED) view.resources.getString(R.string.disconnect)
                else view.resources.getString(R.string.connect)
    }
}