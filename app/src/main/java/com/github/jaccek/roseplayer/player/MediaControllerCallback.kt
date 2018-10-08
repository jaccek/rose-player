package com.github.jaccek.roseplayer.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.dto.toPlayerState
import com.github.jaccek.roseplayer.dto.toSong
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class MediaControllerCallback: MediaControllerCompat.Callback() {

    private val songChangesPublisher = BehaviorProcessor.create<Song>()
    private val playerStateChangesPublisher = BehaviorProcessor.create<PlayingState>()

    val songChanges: Flowable<Song>
        get() = songChangesPublisher

    val playingStateChanges: Flowable<PlayingState>
        get() = playerStateChangesPublisher

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        super.onPlaybackStateChanged(state)
        convertAndPublishPlayerState(state.state)
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        super.onMetadataChanged(metadata)
        convertAndPublishActualSongData(metadata)
    }

    private fun convertAndPublishPlayerState(state: Int) {
        playerStateChangesPublisher.onNext(state.toPlayerState())
    }

    private fun convertAndPublishActualSongData(metadata: MediaMetadataCompat?) {
        metadata?.toSong()?.let {
            songChangesPublisher.onNext(it)
        }
    }
}