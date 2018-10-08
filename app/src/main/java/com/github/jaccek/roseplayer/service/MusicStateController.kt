package com.github.jaccek.roseplayer.service

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.AudioState
import com.github.jaccek.roseplayer.player.MusicPlayer
import io.reactivex.Flowable

class MusicStateController(
    private val player: MusicPlayer
) : MediaSessionCompat.Callback() {

    private val songs = mutableListOf<Song>()
    private var currentSong: Song? = null

    val audioChanges: Flowable<AudioState>
        get() = player.audioChanges

    fun updateSongs(songs: List<Song>) {
        this.songs.clear()
        this.songs.addAll(songs)
    }

    override fun onPlay() {
        super.onPlay()
        onPlayFromMediaId(currentSong?.id?.toString(), null)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)

        val songId = mediaId?.toLong() ?: -1
        currentSong = songs.find { it.id == songId }

        currentSong?.let {
            player.play(it)
        }
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }
}
