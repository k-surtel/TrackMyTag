package com.ks.trackmytag.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.isBleSupported
import com.ks.trackmytag.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentMainBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_main, container, false)
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = DevicesAdapter(ClickListener { device ->
            /// viewModel.onCardClicked(card)
        })
        binding.devices.adapter = adapter

        viewModel.scanResults.observe(viewLifecycleOwner) {
            viewModel.onScanResponseReceived(it)
        }

        viewModel.connectionResponse.observe(viewLifecycleOwner) {

        }

        viewModel.showScanErrorMessage.observe(viewLifecycleOwner) {
            showScanErrorMessage(it)
        }

        viewModel.showScanDevices.observe(viewLifecycleOwner) {
            showScanDevices(it)
        }






        viewModel.savedDevices.observe(viewLifecycleOwner) {
            it?.let { adapter.submitList(it) }
        }

        viewModel.deviceChanged.observe(viewLifecycleOwner) {
            it?.let { adapter.notifyItemChanged(it) }
        }

        if(isBleSupported(requireContext())) { requestBluetoothEnable() }
        requestLocationPermission() //TODO dialog w/ explanation
        loadSettings()

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.action_add -> {
            viewModel.getNewDevices()
            true
        }
        R.id.action_settings -> {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadSettings() {
//        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
//        viewModel.scanService.scanningTime = preferences.getString("scan_time", "5000")!!.toLong()
//
//        preferences.registerOnSharedPreferenceChangeListener { function, key ->
//            if(key.equals("scan_time")) {
//                viewModel.scanService.scanningTime = function.getString("key", "5000")!!.toLong()
//            }
//        }
    }

    private fun requestBluetoothEnable() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) { viewModel.setupBle() }
        }
        resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun requestLocationPermission() {
        if ((checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_DENIED)) {
            val resultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            resultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun showScanErrorMessage(errorCode: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.scanning_error)
            .setMessage(resources.getString(R.string.scanning_error_message, errorCode))
            .show()
    }

    private fun showScanDevices(devices: Map<String, String>) {
        if(devices.isEmpty()) {
            Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show()
        } else {
            var devicesArray: Array<CharSequence> = emptyArray()

            devices.forEach {
                devicesArray = devicesArray.plus(getString(R.string.new_devices_data, it.key, it.value))
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_device)
                .setItems(devicesArray) { _, which ->
                    chooseDevice(which)
                }
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
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
    }
}