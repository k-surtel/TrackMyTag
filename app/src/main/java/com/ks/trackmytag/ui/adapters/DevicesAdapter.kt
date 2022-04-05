package com.ks.trackmytag.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.DeviceStates
import com.ks.trackmytag.data.State
import com.ks.trackmytag.databinding.ItemDeviceBinding

class DevicesAdapter(private val deviceStates: DeviceStates, private val clickListener: DeviceClickListener) :
    ListAdapter<Device, DevicesAdapter.ViewHolder>(EntriesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), deviceStates, clickListener)

    class ViewHolder private constructor(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device, deviceStates: DeviceStates, clickListener: DeviceClickListener) {
            binding.device = device
            binding.deviceStates = deviceStates
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeviceBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
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

class DeviceClickListener(
    val connectClickListener: (device: Device) -> Unit,
    val alarmClickListener: (device: Device) -> Unit,
    val deleteClickListener: (device: Device) -> Unit
) {
    fun onConnectClick(device: Device) = connectClickListener(device)
    fun onAlarmClick(device: Device) = alarmClickListener(device)
    fun onDeleteClick(device: Device) = deleteClickListener(device)
}