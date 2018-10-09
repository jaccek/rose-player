package com.github.jaccek.roseplayer

import android.content.Context
import android.media.MediaPlayer
import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.MusicPlayer
import com.github.jaccek.roseplayer.player.AudioState
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import java.lang.ref.WeakReference

class MusicPlayerImpl(
    context: Context
) : MusicPlayer {

    private val contextRef = WeakReference(context)
    private var player: MediaPlayer? = null
    private var currentSong: Song? = null

    private val audioChangesPublisher = BehaviorProcessor.create<AudioState>()

    override val audioChanges: Flowable<AudioState>
        get() = audioChangesPublisher

    override fun play(song: Song) {
        if (currentSong == song && player?.isPaused == true) {
            resume()
        } else {
            playNewSong(song)
        }
    }

    private fun resume() {
        player?.start()
        currentSong?.let {
            audioChangesPublisher.onNext(AudioState(it, PlayingState.PLAYING))
        }
    }

    private fun playNewSong(song: Song) {
        releasePlayer()
        currentSong = song

        contextRef.get()?.let { context ->
            player = MediaPlayer()
            player?.setDataSource(context, song.uri)
            player?.prepare()
            player?.start()

            audioChangesPublisher.onNext(AudioState(song, PlayingState.PLAYING))

            player?.setOnCompletionListener {
                // TODO: temporary - should go to next song
                audioChangesPublisher.onNext(AudioState(song, PlayingState.STOPPED))
            }
        }
    }

    override fun pause() {
        player?.pause()

        currentSong?.let {
            audioChangesPublisher.onNext(AudioState(it, PlayingState.PAUSED))
        }
    }

    override fun stop() {
        releasePlayer()

        currentSong?.let {
            audioChangesPublisher.onNext(AudioState(it, PlayingState.STOPPED))
        }
    }

    private fun releasePlayer() {
        player?.stop()
        player?.release()

        player = null
        currentSong = null
    }

    private val MediaPlayer.isPaused
        get() = !isPlaying && currentPosition < duration
}
