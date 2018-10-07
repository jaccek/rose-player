package com.github.jaccek.roseplayer.dto

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat

fun Cursor.toSong(): Song {
    val id = getLong(getColumnIndex(MediaStore.Audio.Media._ID))

    return Song(
        id = id,
        title = getString(getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
        uri = id.toMediaUri()
    )
}

private fun Long.toMediaUri(): Uri =
    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this)

fun Song.toMetadata(): MediaMetadataCompat =
    MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.id.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.uri.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
        .build()

fun MediaMetadataCompat.toSong(): Song? =
    if (description?.mediaId == null) {
        null
    } else {
        description?.toSong()
    }


fun MediaBrowserCompat.MediaItem.toSong(): Song =
    this.description.toSong()

private fun MediaDescriptionCompat.toSong(): Song =
    Song(
        id = mediaId?.toLong() ?: -1,
        title = title?.toString() ?: "",
        uri = mediaUri ?: Uri.EMPTY
    )

fun Song.toMediaItem(): MediaBrowserCompat.MediaItem =
    MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(id.toString())
            .setMediaUri(uri)
            .build(),
        0
    )
