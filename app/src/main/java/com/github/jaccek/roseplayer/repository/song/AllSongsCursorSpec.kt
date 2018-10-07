package com.github.jaccek.roseplayer.repository.song

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.github.jaccek.roseplayer.dto.Song
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
                        val title =
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                        val id = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID))
                        val songUri =
                            ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        // Add code to get more column here

                        val song = Song(
                            id = id,
                            title = title,
                            uri = songUri
                        )

                        songs.add(song)
                    }

                }
            }
            cursor?.close()
            Maybe.just(songs)
        }
    }
}
