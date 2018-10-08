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
        // TODO: resume if current song is same as song and it's not finished
        stop()  // TODO: internal stop to avoid sending stop audioState
        currentSong = song

        contextRef.get()?.let { context ->
            player = MediaPlayer()
            player?.setDataSource(context, song.uri)
            player?.prepare()
            player?.start()

            audioChangesPublisher.onNext(AudioState(song, PlayingState.PLAYING))

            player?.setOnCompletionListener {
                // TODO: temporary
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
        player?.stop()
        player?.release()

        currentSong?.let {
            audioChangesPublisher.onNext(AudioState(it, PlayingState.STOPPED))
        }

        player = null
        currentSong = null
    }
}
