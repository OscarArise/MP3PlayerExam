package com.example.mp3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.activity.ComponentActivity

class SoundPlayerActivity : ComponentActivity() {

    private lateinit var soundTitle: TextView
    private lateinit var playButton: MaterialButton
    private lateinit var pauseButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var stateText: TextView
    private lateinit var coverImage: ImageView

    private var resId: Int = 0
    private var titleStr: String = "Sin título"
    private var coverResId: Int = R.drawable.sound_icon
    private var isPlaying: Boolean = false

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state") ?: "Detenido"
            stateText.text = state

            // Cambiar el ícono según el estado
            when (state) {
                "Reproduciendo" -> {
                    playButton.isEnabled = false
                    pauseButton.isEnabled = true
                }
                "Pausado" -> {
                    playButton.isEnabled = true
                    pauseButton.isEnabled = false
                }
                "Detenido" -> {
                    playButton.isEnabled = true
                    pauseButton.isEnabled = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sound_player)

        soundTitle = findViewById(R.id.SoundTitle)
        playButton = findViewById(R.id.btnPlay)
        pauseButton = findViewById(R.id.btnPause)
        stopButton = findViewById(R.id.btnStop)
        stateText = findViewById(R.id.tvState)
        coverImage = findViewById(R.id.imgCoverArt)

        // Recuperar datos
        titleStr = intent.getStringExtra("title") ?: "Sin título"
        resId = intent.getIntExtra("resId", 0)
        coverResId = intent.getIntExtra("coverResId", R.drawable.sound_icon)

        soundTitle.text = titleStr
        coverImage.setImageResource(coverResId)

        // Reproduce automáticamente al abrir
        if (resId != 0) {
            val startIntent = Intent(this, SoundService::class.java).apply {
                action = SoundService.ACTION_PLAY
                putExtra(SoundService.EXTRA_RES_ID, resId)
                putExtra(SoundService.EXTRA_TITLE, titleStr)
                putExtra(SoundService.EXTRA_COVER_ID, coverResId)
            }
            startService(startIntent)
        }

        // Botón Play/Pause
        playButton.setOnClickListener {
            val intent = Intent(this, SoundService::class.java).apply {
                action = SoundService.ACTION_PLAY
                putExtra(SoundService.EXTRA_RES_ID, resId)
                putExtra(SoundService.EXTRA_TITLE, titleStr)
                putExtra(SoundService.EXTRA_COVER_ID, coverResId)
            }
            startService(intent)
            playButton.isEnabled = false
            pauseButton.isEnabled = true
        }

        pauseButton.setOnClickListener {
            val intent = Intent(this, SoundService::class.java).apply {
                action = "com.example.mp3.ACTION_PAUSE"
            }
            startService(intent)
            playButton.isEnabled = true
            pauseButton.isEnabled = false
        }

        // Botón Stop
        stopButton.setOnClickListener {
            val intent = Intent(this, SoundService::class.java).apply {
                action = SoundService.ACTION_STOP
            }
            startService(intent)
            playButton.isEnabled = true
            pauseButton.isEnabled = false
        }


        val filter = IntentFilter("com.example.mp3.UPDATE_UI")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateReceiver)
    }
}
