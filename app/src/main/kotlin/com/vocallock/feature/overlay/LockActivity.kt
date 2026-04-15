package com.vocallock.feature.overlay

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vocallock.core.ui.theme.VocalLockTheme
import com.vocallock.service.VocalLockAccessibilityService
import java.util.Locale

class LockActivity : ComponentActivity(), RecognitionListener {

    private var isVoiceListening by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var voiceStatusText by mutableStateOf("Tap mic to speak phrase")

    private lateinit var intentPackageName: String
    private lateinit var intentAppName: String
    private lateinit var intentAuthType: String
    private lateinit var intentTargetPhrase: String
    private lateinit var intentCorrectPasswordBytes: ByteArray
    private var isUsingMasterPassword: Boolean = false
    private var hasVoice: Boolean = false

    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setTaskDescription(
            ActivityManager.TaskDescription(
                "Locked \uD83D\uDD12",
                null,
                android.graphics.Color.BLACK
            )
        )
        enableEdgeToEdge()

        intentPackageName = intent.getStringExtra("PACKAGE_NAME") ?: return finish()
        intentAppName = intent.getStringExtra("APP_NAME") ?: "App"
        intentAuthType = intent.getStringExtra("AUTH_TYPE") ?: "TEXT_PASSWORD"
        intentTargetPhrase = intent.getStringExtra("TARGET_PHRASE") ?: ""
        intentCorrectPasswordBytes = intent.getByteArrayExtra("PASSWORD_BYTES") ?: ByteArray(0)
        isUsingMasterPassword = intent.getBooleanExtra("USED_MASTER_PASSWORD", false)

        hasVoice = intentTargetPhrase.isNotBlank()
        isVoiceListening = hasVoice

        setContent {
            VocalLockTheme {
                BackHandler { forceAppToBackground() }

                GlassLockOverlay(
                    appName = intentAppName,
                    packageName = intentPackageName,
                    authType = intentAuthType,
                    hasVoice = hasVoice,
                    isListeningForVoice = isVoiceListening,
                    voiceStatusText = voiceStatusText,
                    errorMessage = errorMessage,
                    onPasswordSubmit = { entered ->
                        if (entered == String(intentCorrectPasswordBytes)) {
                            unlockAndClose()
                        } else {
                            errorMessage = "Incorrect Password"
                        }
                    },
                    onMicClick = {
                        if (hasVoice) {
                            if (isVoiceListening) {
                                stopListening()
                                voiceStatusText = "Tap mic to speak phrase"
                            } else {
                                startListening()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun unlockAndClose() {
        VocalLockAccessibilityService.sessionUnlockedApps.add(intentPackageName)
        VocalLockAccessibilityService.activeLockScreenPackage = null

        val unlockIntent = Intent("com.vocallock.UNLOCK_APP").apply {
            setPackage(packageName)
            putExtra("UNLOCKED_PACKAGE", intentPackageName)
            putExtra("USED_MASTER_PASSWORD", isUsingMasterPassword)
        }
        sendBroadcast(unlockIntent)

        val launchIntent = packageManager.getLaunchIntentForPackage(intentPackageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(launchIntent)
        }

        finishAndRemoveTask()
    }

    override fun onResume() {
        super.onResume()
        if (hasVoice && isVoiceListening) {
            startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        stopListening()
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing) forceAppToBackground()
    }

    private fun forceAppToBackground() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    // --- Foreground Speech Engine ---

    private fun startListening() {
        speechRecognizer?.destroy()

        isVoiceListening = true
        errorMessage = null
        voiceStatusText = "Listening..."

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(this)

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                1500L
            )
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
        }

        speechRecognizer?.startListening(speechIntent)
    }

    private fun stopListening() {
        isVoiceListening = false
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onError(error: Int) {
        isVoiceListening = false
        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            voiceStatusText = "Nothing heard. Try again."
        } else {
            voiceStatusText = "Mic error. Try again."
        }
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            processMatches(matches)
        } else {
            isVoiceListening = false
            voiceStatusText = "Nothing heard. Try again."
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            voiceStatusText = "Processing audio..."
        }
    }

    private fun processMatches(matches: List<String>) {
        val phrase = intentTargetPhrase.trim().lowercase()
        val isMatch = matches.any { it.trim().lowercase().contains(phrase) }

        if (isMatch) {
            voiceStatusText = "Phrase Correct! Unlocking..."
            stopListening()
            unlockAndClose()
        } else {
            voiceStatusText = "Incorrect Phrase. Try again."
            isVoiceListening = false
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}