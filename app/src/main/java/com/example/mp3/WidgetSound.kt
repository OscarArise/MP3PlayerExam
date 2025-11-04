package com.example.mp3

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class WidgetSound : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray){
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_sound)

            val toggleIntent = Intent(context, SoundService::class.java).apply {
                action = SoundService.ACTION_TOGGLE
            }

            val togglePi = PendingIntent.getService(
                context, 0, toggleIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btnUpdate, togglePi)

            val stopIntent = Intent(context, SoundService::class.java).apply {
                action = SoundService.ACTION_STOP
            }

            val stopPi = PendingIntent.getService(
                context, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btnStop, stopPi)

            val openIntent = Intent(context, MainActivity::class.java)
            val openPi = PendingIntent.getActivity(
                context, 2, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.imgWidgetArt, openPi)



            manager.updateAppWidget(id, views)
        }
    }
}