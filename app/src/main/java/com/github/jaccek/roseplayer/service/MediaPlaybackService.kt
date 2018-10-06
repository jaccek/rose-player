package com.github.jaccek.roseplayer.service

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.github.jaccek.roseplayer.MusicPlayer
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.presentation.notification.NotificationCreator
import org.koin.android.ext.android.inject


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        const val MEDIA_ROOT_ID = "MEDIA_ROOT_ID"
        private const val MEDIA_SESSION_TAG = "MEDIA_SESSION_TAG"
        private const val NOTIFICATION_ID = 2
    }

    private lateinit var mediaSession: MediaSessionCompat

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
                        val notification = notificationCreator.createPlayerNotification(it, PlayerController.State.PLAYING)
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
                val notification = notificationCreator.createPlayerNotification(it, PlayerController.State.STOPPED)
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
                val notification = notificationCreator.createPlayerNotification(it, PlayerController.State.PAUSED)
                startForeground(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        player = MusicPlayer.getInstance(applicationContext)

        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)
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
            if (it.action == "action") {
                callback.onPause()
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

        val mediaItems = readDataExternal()
            .map {
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setTitle(it.title)
                        .setMediaId(it.id.toString())
                        .setMediaUri(it.uri)
                        .build(),
                    0
                )
            }
            .toMutableList()

        result.sendResult(mediaItems)
    }

    private fun readDataExternal(): List<Song> {
        // TODO: request permission
        val cr = applicationContext?.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val cursor = cr?.query(uri, null, selection, null, sortOrder)

        cursor?.let { cur ->
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    val title =
                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    val id = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID))
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    // Add code to get more column here

                    val song = Song(
                        id = id,
                        title = title,
                        uri = uri
                    )

                    songs.add(song)
                }

            }
            cur.close()
        }
        return songs
    }

    private fun Song.toMetadata(): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.id.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.uri.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
            .build()
    }
}