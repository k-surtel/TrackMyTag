package com.ks.trackmytag.ui.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.connection.OnConnectionStateChangeListener
import com.ks.trackmytag.databinding.FragmentMainBinding
import com.ks.trackmytag.bluetooth.scanning.OnScanFinishedListener


class MainFragment : Fragment() {

    //private val PERMISSION_REQUEST_FINE_LOCATION = 1

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]
        setHasOptionsMenu(true)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.setOnScanFinishedListener(object: OnScanFinishedListener() {
            override fun onScanStarted() {
                Toast.makeText(context, "Rozpoczęto skanowanie", Toast.LENGTH_SHORT).show()
            }

            override fun onScanFinished(devices: MutableList<BluetoothDevice>?, errorCode: Int?) {
                Toast.makeText(context, "Zakończono skanowanie", Toast.LENGTH_SHORT).show()
                displayDevices(devices, errorCode)
            }
        })

        val adapter = DevicesAdapter(ClickListener { device ->
            /// viewModel.onCardClicked(card)
        })
        binding.devices.adapter = adapter

        viewModel.devices.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
            }
        }

        viewModel.deviceChanged.observe(viewLifecycleOwner) {
            it?.let {
                adapter.notifyItemChanged(it)
            }
        }

        loadSettings()

        if (isBleSupported()) viewModel.scanService.setupBle()
        if (!viewModel.scanService.isBleInitialized()) requestBluetoothEnable()
        //requestLocationPermission()

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
            alertBuilder.setTitle("Brak BLE")
            alertBuilder.setMessage("To urządzenie nie wspiera technologii Bluetooth Low Energy.")
            alertBuilder.setPositiveButton(android.R.string.ok, null)
            alertBuilder.setOnDismissListener { requireActivity().finish() }
            alertBuilder.show()
            return false
        } else return true
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        //startActivityForResult(enableBtIntent, 1) //deprecated

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // operations..
            }
        }
        resultLauncher.launch(enableBtIntent)
    }

//    private fun requestLocationPermission() {
//        if ((checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PermissionChecker.PERMISSION_DENIED)
//        ) {
//            requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
//                PERMISSION_REQUEST_FINE_LOCATION
//            )
//        }
//    }

    private fun displayDevices(devices: MutableList<BluetoothDevice>?, errorCode: Int?) {
        if (errorCode != null) {
            //TODO: display error message
        }

        var devicesList = mutableListOf<BluetoothDevice>()

        if (!devices.isNullOrEmpty()) {
            devicesList = viewModel.sortDevices(devices)
        }

        if (!devicesList.isNullOrEmpty()) {

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Wybierz urządzenie")
            alertDialogBuilder.setItems(formatDisplayedDeviceData(devicesList)) { dialog, which ->
                chooseDevice(devicesList.get(which), viewModel)
            }
            alertDialogBuilder.create().show()
        } else Toast.makeText(context, "Nie znaleziono nowych urządzeń", Toast.LENGTH_SHORT).show()
    }

    private fun formatDisplayedDeviceData(devices: MutableList<BluetoothDevice>?): Array<CharSequence> {
        var items: Array<CharSequence> = emptyArray()
        devices?.forEach { items = items.plus(getString(R.string.device_data, it.name, it.address)) }
        return items
    }

    private fun chooseDevice(device: BluetoothDevice, viewModel: MainViewModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Podaj nazwę urządzenia")
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        alertDialogBuilder.setView(input)
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            viewModel.addDevice(device, input.text.toString())
        }
        alertDialogBuilder.setNegativeButton("Anuluj") { dialog, which -> dialog.cancel() }
        alertDialogBuilder.show()
    }
}