package com.github.jaccek.roseplayer.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import com.github.jaccek.roseplayer.dto.*
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.presentation.notification.NotificationCreator
import com.github.jaccek.roseplayer.presentation.notification.PlayerNotification
import com.github.jaccek.roseplayer.repository.Repository
import com.github.jaccek.roseplayer.repository.song.SongsSpecificationFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject


class MediaPlaybackService : MediaBrowserServiceCompat(),
    MusicStateController.PlayerStateChangeListener {

    companion object {
        const val MEDIA_ROOT_ID = "MEDIA_ROOT_ID"
        private const val NOTIFICATION_ID = 2
    }

    private val mediaSession: MediaSessionCompat by inject()

    private val songsRepo: Repository<Song> by inject()
    private val songsSpecFactory: SongsSpecificationFactory by inject()

    private val notificationCreator: NotificationCreator by inject()

    private lateinit var musicController: MusicStateController

    override fun onCreate() {
        super.onCreate()

        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        musicController = MusicStateController(applicationContext, this)
        mediaSession.setCallback(musicController)

        val playbackStateBuilder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setPlaybackState(playbackStateBuilder.build())

        sessionToken = mediaSession.sessionToken
    }

    override fun onPlayerStateChanged(song: Song, state: PlayerState) {
        updateMediaSession(state, song)
        updateNotification(song, state)

        if (state == PlayerState.STOPPED) {
            stopService()
        }
    }

    private fun updateMediaSession(state: PlayerState, song: Song) {
        mediaSession.isActive = state == PlayerState.PLAYING || state == PlayerState.PAUSED
        mediaSession.setMetadata(song.toMetadata())
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state.toPlaybackState(), 0, 1.0f)
                .build()
        )
    }

    private fun updateNotification(song: Song, state: PlayerState) {
        val notification = notificationCreator.createPlayerNotification(song, state)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopService() {
        stopSelf()
        stopForeground(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == PlayerNotification.PLAY_PAUSE_ACTION) {
                if (mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    musicController.onPause()
                } else {
                    musicController.onPlay()  // TODO: this starts playing song from the beginning (not resuming it)
                }
            }
        }
        return Service.START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return MediaBrowserServiceCompat.BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (!TextUtils.equals(MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null)
            return
        }

        result.detach()

        val allSongsSpec = songsSpecFactory.createAllSongsSpecyfication()
        val disposable = songsRepo.query(allSongsSpec)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { musicController.updateSongs(it) }
            .flatMapObservable { Observable.fromIterable(it) }
            .map { it.toMediaItem() }
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result.sendResult(it) },
                { Log.e("MediaPlaybackService", "songs repo error", it) }
            )
    }
}