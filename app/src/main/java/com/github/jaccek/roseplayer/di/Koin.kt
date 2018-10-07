package com.github.jaccek.roseplayer.di

import android.support.v4.media.session.MediaSessionCompat
import com.github.jaccek.roseplayer.dto.Song
import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.player.PlayerControllerImpl
import com.github.jaccek.roseplayer.presentation.notification.NotificationCreator
import com.github.jaccek.roseplayer.repository.Repository
import com.github.jaccek.roseplayer.repository.song.SongsCursorRepo
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    factory<PlayerController> { PlayerControllerImpl(androidContext()) }

    factory { NotificationCreator(androidContext(), get()) }

    single { MediaSessionCompat(androidContext(), "RosePlayer MediaSession") }

    single<Repository<Song>> { SongsCursorRepo(androidContext()) }
}
