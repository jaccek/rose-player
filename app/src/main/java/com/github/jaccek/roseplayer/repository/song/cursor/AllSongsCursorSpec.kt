package com.github.jaccek.roseplayer.repository.song.cursor

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.dto.toSong
import com.github.jaccek.roseplayer.repository.cursor.CursorSpecification
import io.reactivex.Maybe

class AllSongsCursorSpec : CursorSpecification<Song> {

    override fun query(contentResolver: ContentResolver): Maybe<List<Song>> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        // TODO: refactor
        return Maybe.defer {
            val cursor = contentResolver.query(uri, null, selection, null, sortOrder)
            val songs = mutableListOf<Song>()

            cursor?.let { cur ->
                if (cur.count > 0) {
                    while (cur.moveToNext()) {
                        songs.add(cur.toSong())
                    }
                }
            }
            cursor?.close()
            Maybe.just(songs)
        }
    }
}
