package com.github.jaccek.roseplayer.player

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.service.MediaPlaybackService
import com.github.jaccek.roseplayer.service.MediaPlaybackService.Companion.MEDIA_ROOT_ID
import io.reactivex.Flowable

class PlayerControllerImpl(
    private val context: Context
) : PlayerController, MediaBrowserCompat.ConnectionCallback() {

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaPlaybackService::class.java),
        this,
        null
    )

    private lateinit var mediaController: MediaControllerCompat

    private val controllerCallback = MediaControllerCallback()
    private val subscriptionCallback = MediaBrowserSubscriptionCallback()

    override val songChanges: Flowable<Song>
        get() = controllerCallback.songChanges

    override val playingStateChanges: Flowable<PlayingState>
        get() = controllerCallback.playingStateChanges

    override val queueChanges: Flowable<List<Song>>
        get() = subscriptionCallback.queueChanges

    override fun connect() {
        mediaBrowser.connect()
        mediaBrowser.subscribe(MEDIA_ROOT_ID, subscriptionCallback)
    }

    override fun disconnect() {
        if (::mediaController.isInitialized) {
            mediaController.unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()
    }

    override fun play(song: Song) {
        mediaController.transportControls.playFromMediaId(song.id.toString(), null)
    }

    override fun resume() {
        mediaController.transportControls.play()
    }

    override fun pause() {
        mediaController.transportControls.pause()
    }

    override fun onConnected() {
        super.onConnected()

        val token = mediaBrowser.sessionToken
        mediaController = MediaControllerCompat(context, token)

        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        controllerCallback.onMetadataChanged(metadata)
        controllerCallback.onPlaybackStateChanged(pbState)

        mediaController.registerCallback(controllerCallback)
    }
}
