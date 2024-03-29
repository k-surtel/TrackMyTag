package com.ks.trackmytag.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.databinding.ItemDeviceIconBinding

class DeviceIconAdapter(private val clickListener: DeviceIconClickListener)
    : ListAdapter<Device, DeviceIconAdapter.ViewHolder>(EntriesDiffCallback()) {

    private var selectedDeviceAddress: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectedDeviceAddress.isNotBlank() && selectedDeviceAddress.equals(getItem(position).address)) {
            holder.bind(getItem(position), clickListener, true)
            selectedDeviceAddress = ""
        } else holder.bind(getItem(position), clickListener)

    }


    class ViewHolder private constructor(private val binding: ItemDeviceIconBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device, deviceIconClickListener: DeviceIconClickListener, selected: Boolean = false) {
            binding.device = device
            binding.selected = selected
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

    fun notifyItemChangedKeepSelection(deviceAddress: String, position: Int) {
        selectedDeviceAddress = deviceAddress
        this.notifyItemChanged(position)
    }
}

class EntriesDiffCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem.address == newItem.address
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem
    }
}

class DeviceIconClickListener(val deviceIconClickListener: (device: Device) -> Unit) {
    fun onDeviceIconClick(device: Device) = deviceIconClickListener(device)
}