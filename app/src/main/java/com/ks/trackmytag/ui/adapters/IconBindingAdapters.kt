package com.ks.trackmytag.ui.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ks.trackmytag.R
import com.ks.trackmytag.data.State

@BindingAdapter("setBackgroundColor")
fun setBackgroundColor(view: View, color: String?) {
    if (color != null) (view as ImageButton).background.setTint(Color.parseColor(color))
}

@BindingAdapter("setIconAlpha")
fun setIconAlpha(view: View, state: State?) {
    if (state == State.CONNECTED) (view as ImageButton).alpha = 1F
    else (view as ImageButton).alpha = 0.3F
}