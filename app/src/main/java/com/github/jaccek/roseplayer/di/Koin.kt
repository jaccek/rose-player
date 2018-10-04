package com.github.jaccek.roseplayer.di

import com.github.jaccek.roseplayer.player.PlayerController
import com.github.jaccek.roseplayer.player.PlayerControllerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    factory<PlayerController> { PlayerControllerImpl(androidContext()) }
}
