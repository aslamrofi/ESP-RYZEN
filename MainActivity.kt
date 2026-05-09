package com.example.ryzen

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ryzen.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isConnected = false

    // Target MAC Address
    private val TARGET_MAC = "83:FB:A0:86:FD:12"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            binding.btnConnect.postDelayed({ connectToBLE() }, 500)
        } else {
            updateTerminal("ERROR: PERMISSION DENIED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // PAIR BUTTON LOGIC
        binding.btnPair.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }

        // CONNECT BUTTON LOGIC
        binding.btnConnect.setOnClickListener {
            if (isConnected) disconnect() else checkPermissionsAndConnect()
        }
    }

    private fun checkPermissionsAndConnect() {
        val required = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (required.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            connectToBLE()
        } else {
            requestPermissionLauncher.launch(required)
        }
    }

    private fun connectToBLE() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            updateTerminal("ERROR: System Permission Missing")
            return
        }

        if (bluetoothAdapter?.isEnabled == false) {
            updateTerminal("ERROR: Turn on Bluetooth first!")
            return
        }

        // ==========================================
        // NEW TERMINAL MESSAGES TO PROVE IT UPDATED
        // ==========================================
        updateTerminal(">>> SYSTEM OVERRIDE: BLE ENGINE v2.0 <<<")
        updateTerminal("Bypassing Sockets. Locking onto MAC: $TARGET_MAC")

        try {
            val device = bluetoothAdapter?.getRemoteDevice(TARGET_MAC)
            if (device == null) {
                updateTerminal("CRITICAL: MAC Address not found by antenna.")
                return
            }

            updateTerminal("Executing GATT Connection...")
            bluetoothGatt = device.connectGatt(this, false, gattCallback)

        } catch (e: Exception) {
            updateTerminal("BLE CRASH: ${e.message}")
        }
    }

    // --- BLE GATT CALLBACK ENGINE ---
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                updateUIStatus("GATT CONNECTED", android.graphics.Color.GREEN)
                updateTerminal("Hardware secured. Scanning for data services...")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                updateUIStatus("STATUS: DISCONNECTED", android.graphics.Color.RED)
                updateTerminal("Hardware dropped the connection.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateTerminal("Services found! Searching for Serial port...")
                enableDataNotifications(gatt)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            if (data != null) {
                val message = String(data)
                runOnUiThread {
                    binding.tvTerminal.append(message)
                    binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
                }
            }
        }
    }

    private fun enableDataNotifications(gatt: BluetoothGatt) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return

        var targetCharacteristic: BluetoothGattCharacteristic? = null

        // 1. Hunt for the famous HM-10/JDY BLE Serial Pipeline (FFE1)
        for (service in gatt.services) {
            for (characteristic in service.characteristics) {
                if (characteristic.uuid.toString().contains("ffe1", ignoreCase = true)) {
                    targetCharacteristic = characteristic
                    updateTerminal("Target Acquired: FFE1 (Standard BLE Serial)")
                    break
                }
            }
            if (targetCharacteristic != null) break
        }

        // 2. Fallback: If FFE1 isn't found, find the first available data stream
        if (targetCharacteristic == null) {
            for (service in gatt.services) {
                for (characteristic in service.characteristics) {
                    val properties = characteristic.properties
                    if ((properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        targetCharacteristic = characteristic
                        updateTerminal("FFE1 not found. Using alternative feed: ${characteristic.uuid.toString().substring(4, 8)}")
                        break
                    }
                }
                if (targetCharacteristic != null) break
            }
        }

        // 3. Lock onto the pipeline and open the data valve
        if (targetCharacteristic != null) {
            gatt.setCharacteristicNotification(targetCharacteristic, true)

            // This specific UUID (2902) tells the hardware to start pushing text to the phone
            val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val descriptor = targetCharacteristic.getDescriptor(cccdUuid)

            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                updateTerminal(">>> SUCCESS: FEED HIJACKED. TAP A CARD! <<<\n")
            } else {
                updateTerminal("ERROR: Pipeline found, but missing data descriptor.")
            }
        } else {
            updateTerminal("WARNING: Connected, but absolutely no data streams exist on this chip.")
        }
    }

    private fun updateUIStatus(text: String, color: Int) {
        runOnUiThread {
            binding.tvStatus.text = text
            binding.tvStatus.setTextColor(color)
            binding.btnConnect.text = if (isConnected) "DISCONNECT" else "CONNECT TO HC-05"
        }
    }

    private fun disconnect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return

        isConnected = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        updateUIStatus("STATUS: DISCONNECTED", android.graphics.Color.RED)
    }

    private fun updateTerminal(msg: String) {
        runOnUiThread {
            binding.tvTerminal.append("\n> $msg")
            binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bluetoothGatt?.close()
        }
    }
}