package com.ks.trackmytag.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.State
import com.ks.trackmytag.databinding.ItemDeviceBinding

class DevicesAdapter(private val connectionStates: ConnectionStates, private val clickListener: ClickListener) :
    ListAdapter<Device, DevicesAdapter.ViewHolder>(EntriesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), connectionStates, clickListener)
    }

    class ViewHolder private constructor(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device, connectionStates: ConnectionStates, clickListener: ClickListener) {
            binding.device = device
//            binding.clickListener = clickListener
            binding.connectionStates = connectionStates
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

class ClickListener(val clickListener: (device: Device) -> Unit) {
    fun onClick(device: Device) = clickListener(device)
}

class ConnectionStates(connectionStates: Map<String, State>) {
    var states = connectionStates
}