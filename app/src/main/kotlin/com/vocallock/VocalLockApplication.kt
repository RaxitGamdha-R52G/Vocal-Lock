package com.vocallock

import android.app.Application
import com.vocallock.core.di.appModule
import com.vocallock.service.NudgeManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class VocalLockApplication : Application() {

    private val nudge: NudgeManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@VocalLockApplication)
            modules(appModule)
        }
        nudge.createChannels()
    }
}