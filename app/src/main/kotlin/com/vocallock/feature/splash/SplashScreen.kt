package com.vocallock.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocallock.core.ui.components.PrivacyPill
import com.vocallock.core.ui.theme.VLColor
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val vm: SplashViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var logoVisible by remember { mutableStateOf(false) }
    var pill1Visible by remember { mutableStateOf(false) }
    var pill2Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150); logoVisible = true
        delay(700); pill1Visible = true
        delay(300); pill2Visible = true
        delay(1600); onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VLColor.MidnightSlate)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = logoVisible,
            enter = fadeIn(tween(600)) + scaleIn(tween(600), 0.82f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = VLColor.TrustGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(88.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = VLColor.TrustGreenDim,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔒", fontSize = 24.sp)
                            }
                        }
                    }
                }
                Text(
                    "Vocal-Lock",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W500,
                    color = VLColor.TextPrimary,
                    letterSpacing = 0.3.sp
                )
                Text(
                    "your voice · your keys · your device",
                    fontSize = 11.sp,
                    color = VLColor.TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (state.ready) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnimatedVisibility(
                    visible = pill1Visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }) {
                    PrivacyPill(text = stringResource(vm.msgs[state.a]), isTeal = true)
                }
                AnimatedVisibility(
                    visible = pill2Visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }) {
                    PrivacyPill(text = stringResource(vm.msgs[state.b]), isTeal = false)
                }
            }
        }
    }
}
