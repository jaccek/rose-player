package com.github.jaccek.roseplayer.player

import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song

data class AudioState(
    val song: Song,
    val state: PlayingState
)
