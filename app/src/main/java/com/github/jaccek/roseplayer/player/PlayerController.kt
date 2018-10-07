package com.github.jaccek.roseplayer.player

import com.github.jaccek.roseplayer.dto.PlayerState
import com.github.jaccek.roseplayer.dto.Song
import io.reactivex.Flowable

interface PlayerController {

    val songChanges: Flowable<Song>
    val playerStateChanges: Flowable<PlayerState>
    @Deprecated("should be in storage")
    val queueChanges: Flowable<List<Song>>

    fun connect()
    fun disconnect()

    fun play(song: Song)
    fun resume()
    fun pause()
}
