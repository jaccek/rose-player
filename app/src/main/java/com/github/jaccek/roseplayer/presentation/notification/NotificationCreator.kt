package com.github.jaccek.roseplayer.presentation.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.jaccek.roseplayer.R
import com.github.jaccek.roseplayer.dto.PlayingState
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.service.MediaPlaybackService

class NotificationCreator(
    private val applicationContext: Context,
    private val mediaSession: MediaSessionCompat
) {
    fun createPlayerNotification(song: Song, state: PlayingState): Notification {
        val controller = mediaSession.controller
        val componentName = ComponentName(applicationContext, MediaPlaybackService::class.java)
        val playPauseButtonDrawableId = when (state) {
            PlayingState.PLAYING -> R.drawable.pause
            PlayingState.PAUSED -> R.drawable.play
            PlayingState.STOPPED -> R.drawable.play
        }

        return NotificationCompat.Builder(applicationContext)   // TODO: create channel!!!
            .setContentTitle(applicationContext.resources.getString(R.string.app_name))
            .setContentText(song.title)
//            .setSubText(description?.description)     // TODO: song filename
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setLargeIcon(description?.iconBitmap)    // TODO: song icon
            .setContentIntent(controller.sessionActivity)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            .addAction(
                NotificationCompat.Action(
                    playPauseButtonDrawableId,
                    applicationContext.getString(R.string.pause),
                    PendingIntent.getService(
                        applicationContext,
                        0,
                        Intent(PlayerNotification.PLAY_PAUSE_ACTION)
                            .apply { component = componentName },
                        0
                    )
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                android.support.v4.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
            .build()
    }
}