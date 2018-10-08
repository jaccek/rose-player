package com.github.jaccek.roseplayer.dto

import android.support.v4.media.session.PlaybackStateCompat

fun PlayingState.toPlaybackState(): Int =
    when (this) {
        PlayingState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
        PlayingState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
        PlayingState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
    }

fun Int.toPlayerState(): PlayingState =
    when (this) {
        PlaybackStateCompat.STATE_PLAYING -> PlayingState.PLAYING
        PlaybackStateCompat.STATE_PAUSED -> PlayingState.PAUSED
        else -> PlayingState.STOPPED
    }
