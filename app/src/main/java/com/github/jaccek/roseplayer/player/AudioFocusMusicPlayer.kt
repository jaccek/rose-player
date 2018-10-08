package com.github.jaccek.roseplayer.player

import android.content.Context
import android.media.AudioManager
import com.github.jaccek.roseplayer.dto.Song
import io.reactivex.Flowable

class AudioFocusMusicPlayer (
    applicationContext: Context,
    private val player: MusicPlayer
): MusicPlayer, AudioManager.OnAudioFocusChangeListener {

    private val audioManager =
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentSong: Song? = null

    override val audioChanges: Flowable<AudioState>
        get() = player.audioChanges

    override fun play(song: Song) {
        currentSong = song

        val result = audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        onAudioFocusChange(result)
    }

    override fun pause() {
        player.pause()
        audioManager.abandonAudioFocus(this)
    }

    override fun stop() {
        player.stop()
        audioManager.abandonAudioFocus(this)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            currentSong?.let {
                player.play(it)
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            pause()
        }
    }
}