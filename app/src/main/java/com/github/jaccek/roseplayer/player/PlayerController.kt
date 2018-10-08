package com.github.jaccek.roseplayer.player

import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song
import io.reactivex.Flowable

interface PlayerController {

    val songChanges: Flowable<Song>
    val playingStateChanges: Flowable<PlayingState>
    @Deprecated("should be in storage")
    val queueChanges: Flowable<List<Song>>

    fun connect()
    fun disconnect()

    fun play(song: Song)
    fun resume()
    fun pause()
}
