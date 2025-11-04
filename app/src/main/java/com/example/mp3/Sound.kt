package com.example.mp3

import android.content.res.Resources
import kotlin.time.Duration

data class Sound(
    val id : Int,
    val title : String,
    val duration: String,
    val resourcesId: Int,
    val coverResId: Int
)
