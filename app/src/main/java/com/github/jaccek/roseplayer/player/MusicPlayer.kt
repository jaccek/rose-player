package com.github.jaccek.roseplayer.player

import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.AudioState
import io.reactivex.Flowable

interface MusicPlayer {

    val audioChanges: Flowable<AudioState>

    fun play(song: Song)
    fun pause()
    fun stop()
}
