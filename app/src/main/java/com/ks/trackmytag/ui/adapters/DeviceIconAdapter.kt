package com.ks.trackmytag.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.databinding.ItemDeviceIconBinding

class DeviceIconAdapter(val clickListener: DeviceIconClickListener)
    : ListAdapter<Device, DeviceIconAdapter.ViewHolder>(EntriesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), clickListener)

    class ViewHolder private constructor(val binding: ItemDeviceIconBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device, deviceIconClickListener: DeviceIconClickListener) {
            binding.device = device
            binding.clickListener = deviceIconClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceIconBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class DeviceIconClickListener(val deviceIconClickListener: (device: Device) -> Unit) {
    fun onDeviceIconClick(device: Device) = deviceIconClickListener(device)
}