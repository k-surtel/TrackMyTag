package com.ks.trackmytag.ui.adapters

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setButtonText")
fun setButtonText(view : View, state : State?) {
    (view as MaterialButton).text = state?.name ?: State.DISCONNECTED.name
}

@BindingAdapter("setButtonColor")
fun setButtonColor(view : View, state : State?) {
    if(state == State.CONNECTED) (view as MaterialButton).setBackgroundColor(view.resources.getColor(R.color.gray))
    else (view as MaterialButton).setBackgroundColor(view.resources.getColor(R.color.purple_500))
}