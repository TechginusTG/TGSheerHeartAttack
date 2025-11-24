package com.github.TechGinus.TGSheerHeartAttack

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView // Import TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope // Import lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var serialBluetooth: SerialBluetooth
    private lateinit var statusTextView: TextView // Declare status TextView

    // Re-add MediaPlayer for local sound playback
    private lateinit var player: MediaPlayer
    private var isPlaying = false

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        serialBluetooth = SerialBluetooth(this)
        statusTextView = findViewById(R.id.status_text) // Initialize status TextView

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // --- Permissions Request ---
        requestBluetoothPermissions()

        // --- Observe Bluetooth Connection State ---
        lifecycleScope.launch {
            serialBluetooth.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Disconnected -> {
                        statusTextView.text = "Disconnected"
                        statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                    }
                    is ConnectionState.Connecting -> {
                        statusTextView.text = "Connecting..."
                        statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.orange))
                    }
                    is ConnectionState.Connected -> {
                        statusTextView.text = "Connected: ${state.deviceName}"
                        statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.green))
                    }
                    is ConnectionState.Error -> {
                        statusTextView.text = "Error: ${state.message}"
                        statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                    }
                }
            }
        }

        // --- Re-add local MediaPlayer logic ---
        player = MediaPlayer.create(this, R.raw.kocchi_wo_miro)
        player.setOnCompletionListener {
            isPlaying = false // Reset flag on completion
        }

        val playBtn: Button = findViewById(R.id.sha)
        playBtn.setOnClickListener {
            clickedPlayBtn()
        }

        // Set listeners for movement buttons
        serialBluetooth.set_listener()
    }

    // Re-add the function to play sound locally
    fun clickedPlayBtn() {
        if (!isPlaying) {
            player.start()
            isPlaying = true
        }
    }

    private fun requestBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                statusTextView.text = "Error: Bluetooth permissions denied"
                statusTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect Bluetooth when activity is destroyed
        lifecycleScope.launch {
            serialBluetooth.disconnect()
        }
        // Release MediaPlayer resources
        if (::player.isInitialized) {
            player.release()
        }
    }
}