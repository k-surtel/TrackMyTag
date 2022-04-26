package com.ks.trackmytag.ui.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.RequestManager
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.databinding.FragmentMainBinding
import com.ks.trackmytag.ui.adapters.DeviceClickListener
import com.ks.trackmytag.ui.adapters.DeviceIconAdapter
import com.ks.trackmytag.ui.adapters.DeviceIconClickListener
import com.ks.trackmytag.ui.adapters.DevicesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "TRACKTAGMainFragment"

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val bluetoothResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) { viewModel.setupBle() }
            }

        val iconsAdapter = DeviceIconAdapter(DeviceIconClickListener {
            viewModel.chooseDevice(it)
        })
        binding.deviceList.adapter = iconsAdapter


        val adapter = DevicesAdapter(viewModel.deviceStates, DeviceClickListener(
            connectClickListener = { viewModel.onConnectionChangeClick(it) },
            alarmClickListener = { viewModel.deviceAlarm(it) },
            deleteClickListener = { onDeleteDeviceClicked(it) }
        ))

        binding.devices.adapter = adapter

        lifecycleScope.launch { viewModel.savedDevices.collectLatest {
            adapter.submitList(it)
            iconsAdapter.submitList(it)
        } }

        lifecycleScope.launch { viewModel.showScanErrorMessage.collectLatest { showScanErrorMessage(it) } }

        lifecycleScope.launch { viewModel.showScanDevices.collectLatest { showFoundDevices(it) } }

        lifecycleScope.launch { viewModel.deviceChanged.collect {
            adapter.notifyItemChanged(it)
            iconsAdapter.notifyItemChanged(it)
        } }

        lifecycleScope.launch { viewModel.requestPermission.collectLatest { requestPermission(permissionResultLauncher, it) } }

        lifecycleScope.launch { viewModel.requestBluetoothEnabled.collectLatest { requestBluetoothEnabled(bluetoothResultLauncher) } }

        if (RequestManager.checkBleSupport(requireContext())) viewModel.handlePermissionsAndBluetooth(requireContext())

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_add -> {
            if (RequestManager.isBleSupported()) viewModel.findDevices()
            else showNoBleToast()
            true
        }
        R.id.action_settings -> {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToPreferencesFragment())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun requestBluetoothEnabled(launcher: ActivityResultLauncher<Intent>) {
        launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun requestPermission(launcher: ActivityResultLauncher<String>, permission: String) {
        launcher.launch(permission)

//        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
//
//            MaterialAlertDialogBuilder(requireContext())
//                .setTitle(R.string.permission_denied)
//                .setMessage(resources.getString(R.string.permission_denied_description))
//                .setPositiveButton(R.string.ok, null)
//                .show()
//        }
    }

    private fun showNoBleToast() {
        Toast.makeText(requireContext(), R.string.no_ble_support, Toast.LENGTH_SHORT).show()
    }

    private fun showScanErrorMessage(errorCode: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.scanning_error)
            .setMessage(resources.getString(R.string.scanning_error_message, errorCode))
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showFoundDevices(devices: Map<String, String>) {
        if (devices.isEmpty()) {
            Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show()
        } else {
            var devicesArray: Array<CharSequence> = emptyArray()

            devices.forEach {
                devicesArray =
                    devicesArray.plus(getString(R.string.new_devices_data, it.key, it.value))
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_device)
                .setItems(devicesArray) { _, which ->
                    chooseDevice(which)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun chooseDevice(index: Int) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.name_device)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.saveDevice(index, input.text.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun onDeleteDeviceClicked(device: Device) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_device_alert)
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteDevice(device)
            }
            .show()
    }
}