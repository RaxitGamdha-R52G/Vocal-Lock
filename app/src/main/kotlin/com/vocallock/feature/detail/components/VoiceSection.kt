package com.vocallock.feature.detail.components

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vocallock.core.permission.PermissionManager
import com.vocallock.core.permission.UiPermissionEvent
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.feature.detail.DetailUiEvent
import com.vocallock.feature.detail.DetailUiState
import com.vocallock.feature.detail.DetailViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun VoiceSection(
    state: DetailUiState,
    viewModel: DetailViewModel,
    permEvents: MutableSharedFlow<UiPermissionEvent>,
    onDictateRequested: (Int) -> Unit
) {
    val permissionManager: PermissionManager = koinInject()
    val scope = rememberCoroutineScope()

    val handleDictationRequest = { targetIndex: Int ->
        if (!permissionManager.hasAudio) {
            scope.launch { permEvents.emit(UiPermissionEvent.RequirePermission(Manifest.permission.RECORD_AUDIO)) }
        } else {
            onDictateRequested(targetIndex)
            viewModel.onEvent(DetailUiEvent.OnRecordVoiceFingerprintClicked)
        }
    }

    Column {
        Text(
            "Voice Biometrics",
            style = MaterialTheme.typography.titleLarge,
            color = VLColor.CeruleanBlue
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Unlock Phrase Field
        VoiceDropdownField(
            label = "Voice Unlock Phrase",
            value = state.voiceUnlockPhrase,
            onValueChange = { viewModel.onEvent(DetailUiEvent.OnVoiceUnlockTyped(it)) },
            onDictateClick = { handleDictationRequest(0) },
            isError = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lock Phrase Field (Optional)
        VoiceDropdownField(
            label = "Voice Lock Phrase (Optional)",
            value = state.voiceLockPhrase,
            onValueChange = { viewModel.onEvent(DetailUiEvent.OnVoiceLockTyped(it)) },
            onDictateClick = { handleDictationRequest(1) },
            isError = false,
            focusColor = VLColor.CrimsonRed
        )

        // Strict Mode ONLY appears if a voice phrase has actually been provided!
        if (state.voiceUnlockPhrase.isNotBlank() || state.voiceLockPhrase.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Strict Mode (Speaker Identity)",
                        color = VLColor.TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Requires your exact voice print, not just the words.",
                        color = VLColor.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = state.isStrict,
                    onCheckedChange = { viewModel.onEvent(DetailUiEvent.OnStrictModeToggled(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VLColor.TrustGreen,
                        checkedTrackColor = VLColor.TrustGreenDim,
                        uncheckedThumbColor = VLColor.TextSecondary,
                        uncheckedTrackColor = VLColor.SurfaceHigh,
                        uncheckedBorderColor = VLColor.Border
                    )
                )
            }
        }
    }
}

@Composable
private fun VoiceDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDictateClick: () -> Unit,
    isError: Boolean,
    focusColor: androidx.compose.ui.graphics.Color = VLColor.TrustGreen
) {
    var expanded by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf("Type") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = VLColor.TextSecondary, style = MaterialTheme.typography.labelMedium)

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        if (inputMode == "Type") "Type Manually" else "Dictate via Mic",
                        color = VLColor.CeruleanBlue
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = VLColor.CeruleanBlue
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(VLColor.SurfaceHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("Type Manually", color = VLColor.TextPrimary) },
                        onClick = { inputMode = "Type"; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Dictate via Mic", color = VLColor.TextPrimary) },
                        onClick = { inputMode = "Dictate"; expanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (inputMode == "Type") {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = focusColor,
                    unfocusedBorderColor = VLColor.BorderEmphasis
                )
            )
        } else {
            OutlinedButton(
                onClick = onDictateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = focusColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, focusColor)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (value.isEmpty()) "Tap to start dictation" else "Dictated: \"$value\"",
                    color = VLColor.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}