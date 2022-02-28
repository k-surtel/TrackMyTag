package com.ks.trackmytag.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.ks.trackmytag.R
import com.ks.trackmytag.databinding.FragmentMainBinding
import com.ks.trackmytag.bluetooth.scanning.OnScanListener


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]
        setHasOptionsMenu(true)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.setOnScanListener(object: OnScanListener() {
            override fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?) {
                Toast.makeText(context, R.string.scanning_finished, Toast.LENGTH_SHORT).show()
                displayDevices(viewModel.sortNewDevices(devices), errorCode)
            }
        })

        val adapter = DevicesAdapter(ClickListener { device ->
            /// viewModel.onCardClicked(card)
        })
        binding.devices.adapter = adapter

        viewModel.devices.observe(viewLifecycleOwner) {
            it?.let { adapter.submitList(it) }
        }

        viewModel.deviceChanged.observe(viewLifecycleOwner) {
            it?.let { adapter.notifyItemChanged(it) }
        }

        loadSettings()

        if (isBleSupported()) viewModel.scanService.setupBle()
        if (!viewModel.scanService.isBluetoothInitialized()) requestBluetoothEnable()
        requestLocationPermission()

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
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.scanService.scanningTime = preferences.getString("scan_time", "5000")!!.toLong()

        preferences.registerOnSharedPreferenceChangeListener { function, key ->
            if(key.equals("scan_time")) {
                viewModel.scanService.scanningTime = function.getString("key", "5000")!!.toLong()
            }
        }
    }

    private fun isBleSupported(): Boolean {
        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            val alertBuilder = AlertDialog.Builder(requireContext())
            alertBuilder.setTitle(R.string.no_ble)
            alertBuilder.setMessage(R.string.no_ble_support)
            alertBuilder.setPositiveButton(android.R.string.ok, null)
            alertBuilder.setOnDismissListener { requireActivity().finish() }
            alertBuilder.show()
            return false
        } else return true
    }

    private fun requestBluetoothEnable() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun requestLocationPermission() {
        if ((checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_DENIED)) {
            val resultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            resultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun displayDevices(newDevices: MutableList<BluetoothDevice>?, errorCode: Int?) {
        if (errorCode != null) {
            //TODO: display error message
        }

        if (!newDevices.isNullOrEmpty()) {

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle(R.string.choose_device)
            alertDialogBuilder.setItems(viewModel.formatDisplayedDeviceData(newDevices)) { dialog, which ->
                chooseDevice(newDevices.get(which), viewModel)
            }
            alertDialogBuilder.create().show()
        } else Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show()
    }

    private fun chooseDevice(device: BluetoothDevice, viewModel: MainViewModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(R.string.name_device)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        alertDialogBuilder.setView(input)
        alertDialogBuilder.setPositiveButton(R.string.ok) { dialog, which ->
            viewModel.addDevice(device, input.text.toString())
        }
        alertDialogBuilder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        alertDialogBuilder.show()
    }
}