package com.github.jaccek.roseplayer.dto

import android.support.v4.media.session.PlaybackStateCompat

fun PlayerState.toPlaybackState(): Int =
    when (this) {
        PlayerState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
        PlayerState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
        PlayerState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
    }

fun Int.toPlayerState(): PlayerState =
    when (this) {
        PlaybackStateCompat.STATE_PLAYING -> PlayerState.PLAYING
        PlaybackStateCompat.STATE_PAUSED -> PlayerState.PAUSED
        else -> PlayerState.STOPPED
    }
