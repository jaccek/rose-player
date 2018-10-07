package com.github.jaccek.roseplayer.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.dto.toSong
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class MediaControllerCallback: MediaControllerCompat.Callback() {

    private val songChangesPublisher = BehaviorProcessor.create<Song>()
    private val playerStateChangesPublisher = BehaviorProcessor.create<PlayerController.State>()

    val songChanges: Flowable<Song>
        get() = songChangesPublisher

    val playerStateChanges: Flowable<PlayerController.State>
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
        when(state) {
            PlaybackStateCompat.STATE_PLAYING -> playerStateChangesPublisher.onNext(PlayerController.State.PLAYING)
            PlaybackStateCompat.STATE_PAUSED -> playerStateChangesPublisher.onNext(PlayerController.State.PAUSED)
            else -> playerStateChangesPublisher.onNext(PlayerController.State.STOPPED)
        }
    }

    private fun convertAndPublishActualSongData(metadata: MediaMetadataCompat?) {
        metadata?.toSong()?.let {
            songChangesPublisher.onNext(it)
        }
    }
}