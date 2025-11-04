package com.example.mp3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class SoundService : Service() {

    companion object{
        const val ACTION_PLAY = "com.example.mp3.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.mp3.ACTION_PAUSE"
        const val ACTION_TOGGLE = "com.example.mp3.ACTION_TOGGLE"
        const val ACTION_STOP = "com.example.mp3.ACTION_STOP"
        const val EXTRA_RES_ID = "extra_res_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_COVER_ID = "extra_cover_id"

        const val CHANNEL_ID = "sound_channel"
        const val NOTIFICATION_ID = 1
    }

    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var isStopped = true

    private var currentResId: Int = 0
    private var currentTitle: String = "Sin titulo"
    private var currentCoverResId: Int = R.drawable.sound_icon

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        createNotificationChannel()
        showNotification("Detenido")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action){
            ACTION_PLAY -> {
                val resId = intent.getIntExtra(EXTRA_RES_ID, 0)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Sin titulo"
                val coverId = intent.getIntExtra(EXTRA_COVER_ID, R.drawable.sound_icon)

                if (resId != 0) playNew(resId, title, coverId)
            }
            ACTION_TOGGLE -> togglePlayBack()
            ACTION_STOP -> stopPlayback()
            ACTION_PAUSE -> pausePlayback()
        }
        updateWidgetUI()
        return START_STICKY
    }

    private fun playNew(resId: Int, title: String, coverId: Int){
        // Siempre prepara el MediaItem si está detenido o es diferente
        if(currentResId != resId || isStopped){
            val uri = Uri.parse("android.resource://$packageName/$resId")
            val mediaItem = MediaItem.fromUri(uri)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            currentResId = resId
            currentTitle = title
            currentCoverResId = coverId
        }

        player?.play()
        isPlaying = true
        isStopped = false
        sendStateBroadcast("Reproduciendo")
        showNotification("Reproduciendo")
        updateWidgetUI()
    }

    private fun togglePlayBack(){
        if(isStopped){
            //  Si está detenido, necesita preparar de nuevo
            if(currentResId != 0){
                val uri = Uri.parse("android.resource://$packageName/$currentResId")
                val mediaItem = MediaItem.fromUri(uri)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()
                isPlaying = true
                isStopped = false
                sendStateBroadcast("Reproduciendo")
                showNotification("Reproduciendo")
            }
        } else if(isPlaying){
            player?.pause()
            isPlaying = false
            sendStateBroadcast("Pausado")
            showNotification("Pausado")
        } else {
            player?.play()
            isPlaying = true
            sendStateBroadcast("Reproduciendo")
            showNotification("Reproduciendo")
        }
    }

    private fun stopPlayback(){
        player?.stop()
        player?.seekTo(0)
        //  NO resetear currentResId para poder reanudar la misma canción
        isPlaying = false
        isStopped = true
        sendStateBroadcast("Detenido")
        showNotification("Detenido")
    }

    private fun pausePlayback(){
        if (isPlaying && !isStopped){
            player?.pause()
            isPlaying = false
            sendStateBroadcast("Pausado")
            showNotification("Pausado")
            updateWidgetUI()
        }
    }
    private fun showNotification(state: String){
        val toggleIntent = Intent(this, SoundService::class.java).apply { action = ACTION_TOGGLE }
        val togglePi = PendingIntent.getService(
            this, 10, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, SoundService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(
            this, 11, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openIntent = Intent(this, SoundPlayerActivity::class.java).apply {
            putExtra("title", currentTitle)
            putExtra("resId", currentResId)
            putExtra("coverResId", currentCoverResId)
        }
        val openPi = PendingIntent.getActivity(
            this, 12, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText("Estado: $state")
            .setSmallIcon(icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, currentCoverResId))
            .setContentIntent(openPi)
            .addAction(icon, if (isPlaying) "Pausar" else "Reproducir", togglePi)
            .addAction(R.drawable.ic_stop, "Detener", stopPi)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateWidgetUI() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, WidgetSound::class.java)
        val views = android.widget.RemoteViews(packageName, R.layout.widget_sound)

        //  Texto dinámico según estado actual
        val stateText = when {
            isPlaying -> "Reproduciendo"
            isStopped -> "Detenido"
            else -> "Pausado"
        }

        //  Actualiza el título con la canción actual
        views.setTextViewText(R.id.tvWidgetTitle, currentTitle)
        views.setTextViewText(R.id.tvMessage, stateText)
        views.setImageViewResource(R.id.imgWidgetArt, currentCoverResId)

        //  Cambia el ícono de play/pause
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        views.setImageViewResource(R.id.btnUpdate, playPauseIcon)

        appWidgetManager.updateAppWidget(componentName, views)
    }


    private fun sendStateBroadcast(state: String){
        val i = Intent("com.example.mp3.UPDATE_UI")
        i.putExtra("state", state)
        sendBroadcast(i)
    }

    private fun createNotificationChannel(){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID, "Reproductor MP3", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}