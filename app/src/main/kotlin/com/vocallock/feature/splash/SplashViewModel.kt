package com.vocallock.feature.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.R
import com.vocallock.data.datastore.proto.SplashPrefs
import com.vocallock.data.datastore.splashPrefsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(private val ctx: Context) : ViewModel() {
    private val _s = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _s.asStateFlow()

    val msgs = listOf(
        R.string.privacy_msg_1, R.string.privacy_msg_2, R.string.privacy_msg_3,
        R.string.privacy_msg_4, R.string.privacy_msg_5, R.string.privacy_msg_6,
        R.string.privacy_msg_7, R.string.privacy_msg_8, R.string.privacy_msg_9,
        R.string.privacy_msg_10,
    )

    init {
        viewModelScope.launch {
            val prefs = ctx.splashPrefsDataStore.data.first()
            val pool = msgs.indices.toMutableList()
                .also { it.removeAll(setOf(prefs.lastIndexA, prefs.lastIndexB)); it.shuffle() }
            val a = pool[0];
            val b = pool[1]
            _s.value = SplashUiState(a, b, true)
            ctx.splashPrefsDataStore.updateData {
                SplashPrefs.newBuilder().setLastIndexA(a).setLastIndexB(b).build()
            }
        }
    }
}