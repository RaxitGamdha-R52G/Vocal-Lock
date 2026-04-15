package com.vocallock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Listens for both standard boot and Direct Boot (before user unlock).
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val nudgeManager: NudgeManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("VocalLock_Boot", "Received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {

            nudgeManager.createChannels()
        }
    }
}