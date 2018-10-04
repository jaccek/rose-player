package com.github.jaccek.roseplayer.dto

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val uri: Uri
)
