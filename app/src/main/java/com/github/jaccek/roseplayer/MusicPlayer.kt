package com.github.jaccek.roseplayer

import android.content.Context
import android.media.MediaPlayer
import com.github.jaccek.roseplayer.dto.Song
import java.lang.ref.WeakReference

class MusicPlayer private constructor(
    context: Context
) {
    interface SongsListener {
        fun onSongStarted(song: Song)
        fun onSongEnd()
    }

    companion object {
        private var instance: MusicPlayer? = null

        fun getInstance(context: Context): MusicPlayer {        // TODO: ugly!!!! - remove singleton
            return if (instance != null) {
                instance!!
            } else {
                val player = MusicPlayer(context)
                instance = player
                player
            }
        }
    }

    private val contextRef = WeakReference(context)
    private var player: MediaPlayer? = null
    private val listeners = mutableListOf<SongsListener>()

    fun play(song: Song) {
        player?.let {
            it.stop()
            it.release()
        }

        contextRef.get()?.let { context ->
            player = MediaPlayer()
            player?.setDataSource(context, song.uri)
            player?.prepare()
            player?.start()

            listeners.forEach { it.onSongStarted(song) }

            player?.setOnCompletionListener {
                listeners.forEach { it.onSongEnd() }
            }
        }
    }

    fun pause() {
        player?.pause()
    }

    fun addListener(listener: SongsListener) {
        listeners.add(listener)
    }

    fun resume() {
        player?.start()
    }
}
