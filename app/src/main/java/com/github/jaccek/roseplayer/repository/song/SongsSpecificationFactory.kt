package com.github.jaccek.roseplayer.repository.song

import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.repository.Specification

interface SongsSpecificationFactory {

    fun createAllSongsSpecyfication(): Specification<Song>
}
