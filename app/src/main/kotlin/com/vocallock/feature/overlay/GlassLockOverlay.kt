package com.vocallock.feature.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.feature.home.components.AsyncAppIcon

@Composable
fun GlassLockOverlay(
    appName: String,
    packageName: String,
    authType: String,
    hasVoice: Boolean,
    isListeningForVoice: Boolean,
    voiceStatusText: String,
    errorMessage: String? = null,
    onPasswordSubmit: (String) -> Unit,
    onMicClick: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }

    val isPin = authType == "PIN"
    val keyboardType = if (isPin) KeyboardType.NumberPassword else KeyboardType.Password

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isListeningForVoice) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulse_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VLColor.MidnightSlate.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(32.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncAppIcon(
                    packageName = packageName,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(VLColor.MidnightSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(VLColor.CrimsonRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = VLColor.TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineLarge,
                color = VLColor.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Secured by Vocal-Lock Vault",
                style = MaterialTheme.typography.bodyMedium,
                color = VLColor.TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (hasVoice) {
                val statusColor = when {
                    voiceStatusText.contains("Correct") -> VLColor.TrustGreen
                    voiceStatusText.contains("Incorrect") || voiceStatusText.contains("error") || voiceStatusText.contains(
                        "Nothing"
                    ) -> VLColor.CrimsonRed

                    else -> VLColor.NeonCyan
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VLColor.MidnightSlate)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = voiceStatusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(if (isListeningForVoice) VLColor.TrustGreenDim else VLColor.SurfaceHigh)
                        .clickable { onMicClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListeningForVoice) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone",
                        tint = if (isListeningForVoice) VLColor.TextPrimary else VLColor.TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Spacer(modifier = Modifier.height(72.dp))
            }

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text(if (isPin) "Enter PIN" else "Enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (passwordInput.isNotBlank()) {
                            onPasswordSubmit(passwordInput)
                        }
                    }
                ),
                singleLine = true,
                isError = errorMessage != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = VLColor.TrustGreen,
                    unfocusedBorderColor = VLColor.BorderEmphasis,
                    errorBorderColor = VLColor.CrimsonRed
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage,
                    color = VLColor.CrimsonRed,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onPasswordSubmit(passwordInput) },
                enabled = passwordInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VLColor.SurfaceHigh,
                    disabledContainerColor = VLColor.Surface
                )
            ) {
                Text(
                    "Unlock",
                    color = if (passwordInput.isNotBlank()) VLColor.TextPrimary else VLColor.TextDisabled
                )
            }
        }
    }
}