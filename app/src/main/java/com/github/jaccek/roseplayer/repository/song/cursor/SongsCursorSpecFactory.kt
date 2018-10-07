package com.github.jaccek.roseplayer.repository.song.cursor

import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.repository.Specification
import com.github.jaccek.roseplayer.repository.song.SongsSpecificationFactory

class SongsCursorSpecFactory : SongsSpecificationFactory {

    override fun createAllSongsSpecyfication(): Specification<Song> {
        return AllSongsCursorSpec()
    }
}