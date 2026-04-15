package com.vocallock.feature.detail.components

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vocallock.core.ui.theme.VLColor

@Composable
fun RecordVoiceDialog(
    phraseToRecord: String,
    onDismiss: () -> Unit,
    onVoiceSaved: (String) -> Unit
) {
    val context = LocalContext.current

    // 0 = Idle, 1 = Recording, 2 = Success, 3 = Error
    var recordingState by remember { mutableIntStateOf(0) }
    var recognizedText by remember { mutableStateOf(phraseToRecord) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Speech Recognizer Setup ─────────────────────────────────────
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (recordingState == 1) recordingState = 0
                }

                override fun onError(error: Int) {
                    recordingState = 3
                    errorMessage = "Could not hear you. Please try again."
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText = matches[0]
                        recordingState = 2
                    } else {
                        recordingState = 3
                        errorMessage = "No speech detected."
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText = matches[0] // Live transcription!
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    // Clean up the mic when the dialog closes
    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }

    // ── UI Animations ───────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (recordingState == 1) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VLColor.Surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Record Voice Phrase",
                style = MaterialTheme.typography.titleLarge,
                color = VLColor.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the mic and speak your phrase.",
                style = MaterialTheme.typography.bodyMedium,
                color = VLColor.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // The Live Transcription Box
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
                    text = if (recordingState == 1 && recognizedText.isBlank()) "Listening..." else "\"${recognizedText}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = VLColor.NeonCyan,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // The Interactive Microphone Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        when (recordingState) {
                            1 -> VLColor.TrustGreenDim   // Recording: Pulsing Green
                            2 -> VLColor.CeruleanBlue    // Success: Solid Blue
                            3 -> VLColor.CrimsonRed      // Error: Red
                            else -> VLColor.SurfaceHigh  // Idle: Dark Grey
                        }
                    )
                    .clickable(enabled = recordingState != 1) {
                        // Start recording!
                        recognizedText = ""
                        errorMessage = null
                        recordingState = 1
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        }
                        speechRecognizer.startListening(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record",
                    tint = if (recordingState == 0) VLColor.TextMuted else VLColor.TextPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Status Text
            Text(
                text = when (recordingState) {
                    0 -> "Tap microphone to start"
                    1 -> "Listening..."
                    2 -> "Voice captured successfully!"
                    3 -> errorMessage ?: "Error occurred"
                    else -> ""
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (recordingState) {
                    2 -> VLColor.TrustGreen
                    3 -> VLColor.CrimsonRed
                    else -> VLColor.TextSecondary
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VLColor.TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onVoiceSaved(recognizedText) },
                    enabled = recordingState == 2 && recognizedText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VLColor.TrustGreen,
                        disabledContainerColor = VLColor.SurfaceHigh
                    )
                ) {
                    Text(
                        "Save Phrase",
                        color = if (recordingState == 2 && recognizedText.isNotBlank()) VLColor.MidnightSlate else VLColor.TextDisabled
                    )
                }
            }
        }
    }
}