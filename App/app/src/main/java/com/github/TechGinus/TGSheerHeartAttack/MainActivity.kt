package com.github.TechGinus.TGSheerHeartAttack

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.View

class MainActivity : AppCompatActivity() {

    private val serialBluetooth = SerialBluetooth()
    private lateinit var player: MediaPlayer
    private var isPlaying = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val connectBtn: Button = findViewById(R.id.connect)

        connectBtn.setOnClickListener { view -> serialBluetooth.handleConnect(view) }








        player = MediaPlayer.create(this, R.raw.kocchi_wo_miro)
        player.setOnCompletionListener {
            isPlaying = false // 재생 완료 시 false로
        }

        val playBtn: Button = findViewById(R.id.sha)

        playBtn.setOnClickListener {
            clickedPlayBtn()
        }
    }

    fun clickedPlayBtn() {
        if (!isPlaying) {
            player.start()
            isPlaying = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티 종료 시 MediaPlayer 해제
        if (::player.isInitialized) {
            player.release()
            isPlaying = false
        }
    }

}