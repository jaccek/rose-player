package com.github.jaccek.roseplayer.service

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import com.github.jaccek.roseplayer.MusicPlayer
import com.github.jaccek.roseplayer.dto.PlayerState
import com.github.jaccek.roseplayer.dto.Song

class MusicStateController(
    applicationContext: Context,
    private val playerStateListener: PlayerStateChangeListener
) : MediaSessionCompat.Callback() {

    private val audioManager =
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // TODO: create audio focus listener
    private val afChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private val player = MusicPlayer.getInstance(applicationContext)

    private val songs = mutableListOf<Song>()
    private var currentSong: Song? =
        null   // TODO: manage state with current song (playing again, resuming etc.)

    fun updateSongs(songs: List<Song>) {
        this.songs.clear()
        this.songs.addAll(songs)
    }

    override fun onPlay() {
        super.onPlay()

        // TODO: resume instead of calling function below
        onPlayFromMediaId(currentSong?.id?.toString(), null)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)

        val result = audioManager.requestAudioFocus(
            afChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            val songId = mediaId?.toLong() ?: -1
            songs.find { it.id == songId }
                ?.let {
                    currentSong = it
                    player.play(it)
                    playerStateListener.onPlayerStateChanged(it, PlayerState.PLAYING)
                }
            // Register BECOME_NOISY BroadcastReceiver
//                registerReceiver(myNoisyAudioStreamReceiver, intentFilter)    // TODO:
        }
    }

    override fun onStop() {
        super.onStop()

        audioManager.abandonAudioFocus(afChangeListener)
//            unregisterReceiver(myNoisyAudioStreamReceiver)    // TODO
        player.pause()
        currentSong?.let {
            playerStateListener.onPlayerStateChanged(it, PlayerState.PAUSED)
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        // unregister BECOME_NOISY BroadcastReceiver
//            unregisterReceiver(myNoisyAudioStreamReceiver)    // TODO
        currentSong?.let {
            playerStateListener.onPlayerStateChanged(it, PlayerState.PAUSED)
        }
    }

    interface PlayerStateChangeListener {
        fun onPlayerStateChanged(song: Song, state: PlayerState)
    }
}
