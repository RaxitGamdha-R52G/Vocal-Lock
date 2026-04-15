package com.vocallock.feature.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vocallock.core.permission.UiPermissionEvent
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.feature.detail.components.DeleteConfirmationDialog
import com.vocallock.feature.detail.components.RecordVoiceDialog
import com.vocallock.feature.detail.components.SecuritySection
import com.vocallock.feature.detail.components.SetPasswordDialog
import com.vocallock.feature.detail.components.VoiceSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    permEvents: MutableSharedFlow<UiPermissionEvent>,
    viewModel: DetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var activeDictationTarget by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DetailUiEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()

                is DetailUiEffect.NavigateBack -> onNavigateBack()
                is DetailUiEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    if (state.showPasswordDialog) {
        SetPasswordDialog(
            authType = state.authType,
            onDismiss = { viewModel.onEvent(DetailUiEvent.OnDismissPasswordDialog) },
            onPasswordConfirmed = { password ->
                viewModel.onEvent(
                    DetailUiEvent.OnPasswordSaved(
                        password
                    )
                )
            }
        )
    }

    if (state.showVoiceRecordDialog) {
        RecordVoiceDialog(
            phraseToRecord = if (activeDictationTarget == 0) state.voiceUnlockPhrase else state.voiceLockPhrase,
            onDismiss = { viewModel.onEvent(DetailUiEvent.OnDismissVoiceDialog) },
            onVoiceSaved = { transcribedText ->
                if (activeDictationTarget == 0) {
                    viewModel.onEvent(DetailUiEvent.OnVoiceUnlockTyped(transcribedText))
                } else {
                    viewModel.onEvent(DetailUiEvent.OnVoiceLockTyped(transcribedText))
                }
                viewModel.onEvent(DetailUiEvent.OnVoicePrintSaved)
            }
        )
    }

    if (state.showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            isGroup = state.isGroup,
            onDismiss = { viewModel.onEvent(DetailUiEvent.OnDismissDeleteDialog) },
            onConfirm = { viewModel.onEvent(DetailUiEvent.OnConfirmDelete) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, color = VLColor.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(DetailUiEvent.OnBackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VLColor.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VLColor.MidnightSlate)
            )
        },
        containerColor = VLColor.MidnightSlate
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VLColor.TrustGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SecuritySection(state, viewModel)

                VoiceSection(
                    state = state,
                    viewModel = viewModel,
                    permEvents = permEvents,
                    onDictateRequested = { target -> activeDictationTarget = target }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.onEvent(DetailUiEvent.OnDeleteClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VLColor.CrimsonRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VLColor.CrimsonRed)
                ) {
                    Text(if (state.isGroup) "Delete Group" else "Remove App from Vault")
                }

                Spacer(modifier = Modifier.height(16.dp))

                val canSave = state.hasSecretSaved && state.voiceUnlockPhrase.isNotBlank()
                Button(
                    onClick = { viewModel.onEvent(DetailUiEvent.OnSaveChanges) },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VLColor.TrustGreen,
                        disabledContainerColor = VLColor.SurfaceHigh
                    )
                ) {
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (canSave) VLColor.MidnightSlate else VLColor.TextDisabled
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}