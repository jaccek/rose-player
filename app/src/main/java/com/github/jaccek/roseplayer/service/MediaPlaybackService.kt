package com.github.jaccek.roseplayer.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import com.github.jaccek.roseplayer.MusicPlayer
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.dto.toMediaItem
import com.github.jaccek.roseplayer.dto.toMetadata
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.presentation.notification.NotificationCreator
import com.github.jaccek.roseplayer.presentation.notification.PlayerNotification
import com.github.jaccek.roseplayer.repository.Repository
import com.github.jaccek.roseplayer.repository.song.SongsSpecificationFactory
import com.github.jaccek.roseplayer.repository.song.cursor.AllSongsCursorSpec
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        const val MEDIA_ROOT_ID = "MEDIA_ROOT_ID"
        private const val NOTIFICATION_ID = 2
    }

    private val mediaSession: MediaSessionCompat by inject()

    private val songsRepo: Repository<Song> by inject()
    private val songsSpecFactory: SongsSpecificationFactory by inject()

    private val notificationCreator: NotificationCreator by inject()

    private val afChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    //    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()  // TODO
    private lateinit var player: MusicPlayer
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val songs = mutableListOf<Song>()
    private var currentSong: Song? =
        null   // TODO: manage state with current song (playing again, resuming etc.)

    private val callback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()

            onPlayFromMediaId(currentSong?.id?.toString(), null)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)

            val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val result = am.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaSession.isActive = true
                val songId = mediaId?.toLong() ?: -1
                songs.find { it.id == songId }
                    ?.let {
                        currentSong = it
                        player.play(it)
                        mediaSession.setMetadata(it.toMetadata())
                        val notification = notificationCreator.createPlayerNotification(
                            it,
                            PlayerController.State.PLAYING
                        )
                        startForeground(NOTIFICATION_ID, notification)
                    }
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                        .build()
                )
                // Register BECOME_NOISY BroadcastReceiver
//                registerReceiver(myNoisyAudioStreamReceiver, intentFilter)    // TODO:
            }
        }

        override fun onStop() {
            super.onStop()

            val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocus(afChangeListener)
//            unregisterReceiver(myNoisyAudioStreamReceiver)    // TODO
            stopSelf()
            mediaSession.isActive = false
            player.pause()
            stopForeground(false)
            currentSong?.let {
                val notification =
                    notificationCreator.createPlayerNotification(it, PlayerController.State.STOPPED)
                startForeground(NOTIFICATION_ID, notification)
            }
        }

        override fun onPause() {
            super.onPause()

            val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                    .build()
            )
            player.pause()
            // unregister BECOME_NOISY BroadcastReceiver
//            unregisterReceiver(myNoisyAudioStreamReceiver)    // TODO
            currentSong?.let {
                val notification =
                    notificationCreator.createPlayerNotification(it, PlayerController.State.PAUSED)
                startForeground(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        player = MusicPlayer.getInstance(applicationContext)

        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        val playbackStateBuilder = PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setPlaybackState(playbackStateBuilder.build())

        mediaSession.setCallback(callback)

        sessionToken = mediaSession.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == PlayerNotification.PLAY_PAUSE_ACTION) {
                if (mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    callback.onPause()
                } else {
                    callback.onPlay()  // TODO: this starts playing song from the beginning (not resuming it)
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
            .doOnSuccess {
                songs.clear()
                songs.addAll(it)
            }
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