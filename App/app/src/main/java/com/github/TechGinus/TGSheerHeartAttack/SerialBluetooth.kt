package com.github.TechGinus.TGSheerHeartAttack

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

// Sealed class to represent the connection state
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class SerialBluetooth(private val activity: AppCompatActivity) {

    // Standard SPP UUID
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothManager: BluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Coroutine scope for Bluetooth operations
    private val scope = CoroutineScope(Dispatchers.Main)

    // Placeholder for ESP32 MAC Address. User needs to replace this with their ESP32's actual MAC address.
    // You can find this address in the ESP32's serial monitor output when Bluetooth is initialized.
    private val ESP32_MAC_ADDRESS = "00:00:00:00:00:00" // !!! REPLACE WITH YOUR ESP32 MAC ADDRESS !!!

    fun set_listener() {
        val goBtn: Button = activity.findViewById(R.id.go_btn)
        goBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingGo(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedGo(view)
                    true
                }
                else -> false
            }
        }

        val backBtn: Button = activity.findViewById(R.id.back_btn)
        backBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingBack(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedBack(view)
                    true
                }
                else -> false
            }
        }

        val turnLeftBtn: Button = activity.findViewById(R.id.turn_left_btn)
        turnLeftBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingTurn_Left(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedTurn_Left(view)
                    true
                }
                else -> false
            }
        }

        val turnRightBtn: Button = activity.findViewById(R.id.turn_right_btn)
        turnRightBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingTurn_Right(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedTurn_Right(view)
                    true
                }
                else -> false
            }
        }

        val connectBtn: Button = activity.findViewById(R.id.connect)
        connectBtn.setOnClickListener { handleConnect(it) }
    }


    @SuppressLint("MissingPermission")
    fun handleConnect(view: View) {
        if (_connectionState.value is ConnectionState.Connected) {
            Snackbar.make(view, "Already connected", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (bluetoothAdapter == null) {
            Snackbar.make(view, "Bluetooth not supported", Snackbar.LENGTH_SHORT).show()
            _connectionState.value = ConnectionState.Error("Bluetooth not supported")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            Snackbar.make(view, "Bluetooth not enabled", Snackbar.LENGTH_SHORT).show()
            _connectionState.value = ConnectionState.Error("Bluetooth not enabled")
            return
        }
        
        // --- Runtime permission check ---
        // BLUETOOTH_CONNECT permission is needed for getRemoteDevice() and createRfcommSocketToServiceRecord()
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permissions should ideally be requested in MainActivity.
            Snackbar.make(view, "Bluetooth Connect permission not granted. Please grant it in settings.", Snackbar.LENGTH_LONG).show()
            _connectionState.value = ConnectionState.Error("Bluetooth Connect permission not granted")
            return
        }

        Snackbar.make(view, "Attempting to connect to $ESP32_MAC_ADDRESS", Snackbar.LENGTH_SHORT).show()
        _connectionState.value = ConnectionState.Connecting

        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(ESP32_MAC_ADDRESS)
                    socket = device.createRfcommSocketToServiceRecord(sppUuid)
                    bluetoothAdapter.cancelDiscovery() // Cancel discovery to speed up connection
                    socket?.connect()
                    outputStream = socket?.outputStream
                    _connectionState.value = ConnectionState.Connected(device.name ?: "Unknown Device")
                    Snackbar.make(view, "Connected to ${device.name}", Snackbar.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Snackbar.make(view, "Connection failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                    _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
                    disconnect() // Ensure resources are closed on failure
                }
            }
        }
    }

    private fun sendCommand(command: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                if (outputStream == null) {
                    _connectionState.value = ConnectionState.Error("Not connected. Cannot send command.")
                    return@withContext
                }
                try {
                    outputStream?.write(command.toByteArray())
                    // Optionally show a Snackbar for sent command, but might be too chatty
                    // Snackbar.make(activity.findViewById(android.R.id.content), "Sent: $command", Snackbar.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    _connectionState.value = ConnectionState.Error("Send failed: ${e.message}")
                    disconnect()
                }
            }
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                // Ignore exceptions on close
            } finally {
                outputStream = null
                socket = null
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    // Existing UI-related methods, now calling sendCommand
    fun pressingGo(view: View) {
        Snackbar.make(view, "pressing GO", Snackbar.LENGTH_SHORT).show()
        sendCommand("F")
    }

    fun releasedGo(view: View) {
        Snackbar.make(view, "released GO", Snackbar.LENGTH_SHORT).show()
        sendCommand("S") // Stop when released
    }

    fun pressingBack(view: View) {
        Snackbar.make(view, "pressing back", Snackbar.LENGTH_SHORT).show()
        sendCommand("B")
    }

    fun releasedBack(view: View) {
        Snackbar.make(view, "released back", Snackbar.LENGTH_SHORT).show()
        sendCommand("S") // Stop when released
    }

    fun pressingTurn_Left(view: View) {
        Snackbar.make(view, "pressing LT", Snackbar.LENGTH_SHORT).show()
        sendCommand("L")
    }

    fun releasedTurn_Left(view: View) {
        Snackbar.make(view, "released LT", Snackbar.LENGTH_SHORT).show()
        sendCommand("S") // Stop when released
    }

    fun pressingTurn_Right(view: View) {
        Snackbar.make(view, "pressing RT", Snackbar.LENGTH_SHORT).show()
        sendCommand("R")
    }

    fun releasedTurn_Right(view: View) {
        Snackbar.make(view, "released RT", Snackbar.LENGTH_SHORT).show()
        sendCommand("S") // Stop when released
    }
}