package com.ks.trackmytag.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.isBleSupported
import com.ks.trackmytag.databinding.FragmentMainBinding
import com.ks.trackmytag.bluetooth.scanning.OnScanListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        val application = requireNotNull(this.activity).application
        //val viewModelFactory = MainViewModelFactory(application)
        //viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = DevicesAdapter(ClickListener { device ->
            /// viewModel.onCardClicked(card)
        })
        binding.devices.adapter = adapter

        viewModel.setOnScanListener(object: OnScanListener() {
            override fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?) {
                Toast.makeText(context, R.string.scanning_finished, Toast.LENGTH_SHORT).show()
                displayDevices(viewModel.sortNewDevices(devices), errorCode)
            }
        })

        viewModel.devices.observe(viewLifecycleOwner) {
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
            viewModel.scan()
            true
        }
        R.id.action_settings -> {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadSettings() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        viewModel.scanService.scanningTime = preferences.getString("scan_time", "5000")!!.toLong()

        preferences.registerOnSharedPreferenceChangeListener { function, key ->
            if(key.equals("scan_time")) {
                viewModel.scanService.scanningTime = function.getString("key", "5000")!!.toLong()
            }
        }
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

    private fun displayDevices(newDevices: MutableList<BluetoothDevice>?, errorCode: Int?) {
        if (errorCode != null && errorCode != 0) {
            //1 - scan already started
            //2 - Fails to start scan as app cannot be registered.
            //3 - Fails to start scan due an internal error
            //4 - Fails to start power optimized scan as this feature is not supported.
            //5 - Fails to start scan as it is out of hardware resources.
            //6 - Fails to start scan as application tries to scan too frequently.
            //TODO

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.scanning_error)
                .setMessage(resources.getString(R.string.scanning_error_message, errorCode))
                .show()
        }

        if (!newDevices.isNullOrEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_device)
                .setItems(viewModel.formatDisplayedDeviceData(newDevices)) { _, which ->
                    chooseDevice(newDevices.get(which), viewModel)
                }
                .show()
        } else Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show()
    }

    private fun chooseDevice(device: BluetoothDevice, viewModel: MainViewModel) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.name_device)
            .setView(input)
            .setPositiveButton(R.string.ok) { dialog, _ -> viewModel.addDevice(device, input.text.toString()) }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
    }
}