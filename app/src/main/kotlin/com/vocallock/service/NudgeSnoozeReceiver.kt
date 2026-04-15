package com.vocallock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vocallock.domain.usecase.SnoozeNudgeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NudgeSnoozeReceiver : BroadcastReceiver(), KoinComponent {

    private val snooze: SnoozeNudgeUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pkg = intent.getStringExtra("pkg") ?: return

        // goAsync() tells Android to keep the BroadcastReceiver alive
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                snooze(pkg)
            } finally {
                pending.finish()
            }
        }
    }
}