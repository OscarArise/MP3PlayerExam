package com.example.mp3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sounds = listOf(
            Sound(1, "What's up Danger", "3:42", R.raw.sound1, R.drawable.whats),
            Sound(2, "WORTHLESS", "2:47", R.raw.sound2, R.drawable.worth),
            Sound(3, "Sunflower", "2:41", R.raw.sound3, R.drawable.whats),
            Sound(4, "Villain (Take the Shot)", "3:09", R.raw.sound4, R.drawable.villain)
        )

        val adapter = SoundAdapter(sounds){ sound ->
            val intent = Intent(this, SoundPlayerActivity::class.java).apply {
                putExtra("title", sound.title)
                putExtra("resId", sound.resourcesId)
                putExtra("coverResId", sound.coverResId)
            }
            startActivity(intent)

            val serviceIntent = Intent(this, SoundService::class.java).apply {
                action = SoundService.ACTION_PLAY   // mejor que ACTION_TOGGLE aquí
                putExtra(SoundService.EXTRA_RES_ID, sound.resourcesId)
                putExtra(SoundService.EXTRA_TITLE, sound.title)
                putExtra(SoundService.EXTRA_COVER_ID, sound.coverResId) // <-- clave
            }
            startService(serviceIntent)

            Toast.makeText(this, "Reproduciendo: ${sound.title}", Toast.LENGTH_SHORT).show()

        }

        recyclerView.adapter = adapter


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }
}

