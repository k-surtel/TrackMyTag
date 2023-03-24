package com.ks.trackmytag.ui.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ks.trackmytag.R
import com.ks.trackmytag.bluetooth.RequestManager
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.databinding.DialogSettingsBinding
import com.ks.trackmytag.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "TRACKTAGMainFragment"

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    object Alert {
        private var alertingDevices = mutableListOf<Device>()
        private lateinit var ringtone: Ringtone
        private lateinit var dialog: AlertDialog

        fun alert(context: Context, device: Device, ringtoneUri: String?) {
            if (alertingDevices.contains(device)) stopAlert(device)
            else if (!ringtoneUri.isNullOrBlank()) {
                ringtone = RingtoneManager.getRingtone(context, Uri.parse(ringtoneUri))
                alertingDevices.add(device)
                ringtone.play()
                dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(device.name)
                    .setCancelable(false)
                    .setNegativeButton(R.string.ok) { _, _ ->
                        stopAlert(device)
                    }.show()
            }

        }

        private fun stopAlert(device: Device) {
            if (alertingDevices.contains(device)) {
                alertingDevices.remove(device)
                if (alertingDevices.isEmpty()) {
                    ringtone.stop()
                    dialog.dismiss()
                }
            }
        }
    }

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

        binding.deviceList.adapter = viewModel.adapter

        binding.settingsButton.setOnClickListener { showItagSettings() }

        lifecycleScope.launch { viewModel.emptyDevicesList.collectLatest { emptyDevicesListUpdateScreen(binding, it) } }

        lifecycleScope.launch { viewModel.showScanErrorMessage.collectLatest { showScanErrorMessage(it) } }

        lifecycleScope.launch { viewModel.showScanDevices.collectLatest { showFoundDevices(it) } }

        lifecycleScope.launch { viewModel.requestPermission.collectLatest { requestPermission(permissionResultLauncher, it) } }

        lifecycleScope.launch { viewModel.requestBluetoothEnabled.collectLatest { requestBluetoothEnabled(bluetoothResultLauncher) } }

        lifecycleScope.launch { viewModel.buttonClick.collectLatest {
            if (it.ringtone.isNotBlank()) {
                val ringtoneUri = getRingtones()[it.ringtone]
                Alert.alert(requireContext(), it, ringtoneUri)
            }
        } }

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
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsActivity())
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

    private fun onDeleteDeviceClicked() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_device_alert)
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteDevice()
            }
            .show()
    }

    private fun showItagSettings() {
        val binding: DialogSettingsBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.dialog_settings, null, false)

        binding.viewModel = viewModel

//        val iconDialog = MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Choose icon") // todo move to string
//            .setView(R.layout.dialog_choose_icon)
//            .setNeutralButton(R.string.cancel, null)

//        binding.icon.setOnClickListener {
//            iconDialog.show()
//        }

        binding.colorButton.setOnClickListener {
            getColorPicker(binding).show()
        }

        binding.deviceRingtone.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) getRingtonesDialog(binding.deviceRingtone)
        }

        binding.deviceRingtone.setOnClickListener {
            getRingtonesDialog(binding.deviceRingtone)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings)
            .setView(binding.root)
            .setNeutralButton(R.string.cancel, null)
            .setNegativeButton(R.string.delete_device) { _, _ ->
                onDeleteDeviceClicked()
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = binding.deviceName.text.toString()
                val color = binding.colorButton.text.toString()
                val ringtone = binding.deviceRingtone.text.toString()
                viewModel.updateDevice(name, color, ringtone)
            }
            .setCancelable(false)
            .show()
    }

    private fun getColorPicker(binding: DialogSettingsBinding): AlertDialog {
        return ColorPickerDialogBuilder
            .with(context)
            .setTitle(R.string.choose_color)
            .initialColor(Color.RED)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .showAlphaSlider(false)
            .setPositiveButton(R.string.ok) { _, selectedColor, allColors ->
                binding.colorButton.background.setTint(selectedColor)
                binding.colorButton.text = "#" + Integer.toHexString(selectedColor)
                if (ColorUtils.calculateLuminance(selectedColor) < 0.5) binding.colorButton.setTextColor(Color.WHITE)
                else binding.colorButton.setTextColor(Color.BLACK)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .build()
    }

    private fun getRingtonesDialog(ringtoneField: TextInputEditText): AlertDialog {
        val xList = getRingtones().keys.toList()
        val xAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner, xList)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_ringtone)
            .setAdapter(xAdapter) { dialog, which ->
                ringtoneField.setText(xList[which])
            }
            .show()
    }

    private fun getRingtones(): Map<String, String> {
        val ringtoneManager = RingtoneManager(activity)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor = ringtoneManager.cursor

        val ringtones = HashMap<String, String>()
        while (cursor.moveToNext()) {
            val ringtoneTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val ringtoneUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            ringtones[ringtoneTitle] = ringtoneUri
        }

        return ringtones
    }

    private fun emptyDevicesListUpdateScreen(binding: FragmentMainBinding, noDevicesSaved: Boolean) {
        if (noDevicesSaved) binding.deviceCard.visibility = View.GONE
        else binding.deviceCard.visibility = View.VISIBLE
    }
}