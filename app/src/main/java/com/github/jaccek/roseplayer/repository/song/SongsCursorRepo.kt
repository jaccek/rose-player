package com.github.jaccek.roseplayer.repository.song

import android.content.Context
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.repository.Repository
import com.github.jaccek.roseplayer.repository.Specification
import com.github.jaccek.roseplayer.repository.cursor.CursorSpecification
import io.reactivex.Maybe
import java.security.InvalidParameterException

class SongsCursorRepo(
    applicationContext: Context
) : Repository<Song> {

    private val contentResolver = applicationContext.contentResolver

    override fun add(item: Song) {
        throw UnsupportedOperationException("Cannot add new song from player")
    }

    override fun update(item: Song) {
        throw UnsupportedOperationException("Cannot update song from player")
    }

    override fun remove(item: Song) {
        throw UnsupportedOperationException("Cannot remove song from player")
    }

    override fun query(spec: Specification<Song>): Maybe<List<Song>> {
        if (spec is CursorSpecification<Song>) {
            return spec.query(contentResolver)
        } else {
            throw InvalidParameterException("Use instance of CursorSpecification with this repo")
        }
    }
}
