package com.github.jaccek.roseplayer

import android.app.Application
import com.github.jaccek.roseplayer.di.appModule
import org.koin.android.ext.android.startKoin

class RosePlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(appModule))
    }
}