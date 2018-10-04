package com.github.jaccek.roseplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.service.MediaPlaybackService
import com.github.jaccek.roseplayer.service.MediaPlaybackService.Companion.MEDIA_ROOT_ID
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

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

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            super.onPlaybackStateChanged(state)
            convertAndPublishPlayerState(state.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.toSong()?.let {
                songChangesPublisher.onNext(it)
            }
        }
    }

    private val songChangesPublisher = PublishProcessor.create<Song>()
    private val playerStateChangesPublisher = PublishProcessor.create<PlayerController.State>()
    private val queueChangesPublisher = PublishProcessor.create<List<Song>>()

    override val songChanges: Flowable<Song>
        get() = songChangesPublisher.onBackpressureLatest()

    override val playerStateChanges: Flowable<PlayerController.State>
        get() = playerStateChangesPublisher.onBackpressureLatest()

    override val queueChanges: Flowable<List<Song>>
        get() = queueChangesPublisher.onBackpressureLatest()

    override fun connect() {
        mediaBrowser.connect()
        mediaBrowser.subscribe(
                MEDIA_ROOT_ID,
                object : MediaBrowserCompat.SubscriptionCallback() {

                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {
                        Log.d("abc", "loaded")
                        super.onChildrenLoaded(parentId, children)
                        val songs = children.map {
                            Song(
                                id = it.description.mediaId?.toLong() ?: -1,
                                title = it.description.title?.toString() ?: "",
                                uri = it.description.mediaUri ?: Uri.EMPTY
                            )
                        }
                        queueChangesPublisher.onNext(songs)
                    }
                })
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

        // Get the token for the MediaSession
        val token = mediaBrowser.sessionToken

        // Create a MediaControllerCompat
        mediaController = MediaControllerCompat(context, token)

        // Display the initial state
        val metadata: MediaMetadataCompat? = mediaController.metadata
        val pbState = mediaController.playbackState

        metadata?.toSong()?.let {
            songChangesPublisher.onNext(it)
        }

        convertAndPublishPlayerState(pbState.state)

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    private fun convertAndPublishPlayerState(state: Int) {
        when(state) {
            PlaybackStateCompat.STATE_PLAYING -> playerStateChangesPublisher.onNext(PlayerController.State.PLAYING)
            PlaybackStateCompat.STATE_PAUSED -> playerStateChangesPublisher.onNext(PlayerController.State.PAUSED)
            else -> playerStateChangesPublisher.onNext(PlayerController.State.STOPPED)
        }
    }

    private fun MediaMetadataCompat.toSong(): Song? {
        if (description?.mediaId == null) {
            return null
        }
        return Song(
            id = description?.mediaId?.toLong() ?: -1,
            title = description?.title?.toString() ?: "",
            uri = description?.mediaUri ?: Uri.EMPTY
        )
    }
}
