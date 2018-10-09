package com.github.jaccek.roseplayer.di

import android.support.v4.media.session.MediaSessionCompat
import com.github.jaccek.roseplayer.MusicPlayerImpl
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.AudioFocusMusicPlayer
import com.github.jaccek.roseplayer.player.MusicPlayer
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.player.PlayerControllerImpl
import com.github.jaccek.roseplayer.presentation.notification.NotificationCreator
import com.github.jaccek.roseplayer.repository.Repository
import com.github.jaccek.roseplayer.repository.song.SongsSpecificationFactory
import com.github.jaccek.roseplayer.repository.song.cursor.SongsCursorRepo
import com.github.jaccek.roseplayer.repository.song.cursor.SongsCursorSpecFactory
import com.github.jaccek.roseplayer.service.MusicStateController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    factory<PlayerController> { PlayerControllerImpl(androidContext()) }

    factory { NotificationCreator(androidContext(), get()) }

    single { MediaSessionCompat(androidContext(), "RosePlayer MediaSession") }

    single<Repository<Song>> {
        SongsCursorRepo(
            androidContext()
        )
    }

    single<SongsSpecificationFactory> { SongsCursorSpecFactory() }

    single<MusicPlayer> {
        val internalPlayer = MusicPlayerImpl(androidContext())
        AudioFocusMusicPlayer(androidContext(), internalPlayer)
    }

    single { MusicStateController(get()) }
}
