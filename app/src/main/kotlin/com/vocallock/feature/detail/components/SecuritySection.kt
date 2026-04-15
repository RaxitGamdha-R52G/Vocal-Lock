package com.vocallock.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.feature.detail.DetailUiEvent
import com.vocallock.feature.detail.DetailUiState
import com.vocallock.feature.detail.DetailViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecuritySection(state: DetailUiState, viewModel: DetailViewModel) {
    Column {
        Text(
            "Security & Organization",
            style = MaterialTheme.typography.titleLarge,
            color = VLColor.CeruleanBlue
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Group Assignment (Only for Apps, not for editing groups themselves)
        if (!state.isGroup) {
            Text(
                "Assign to Group",
                style = MaterialTheme.typography.labelMedium,
                color = VLColor.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // FlowRow makes the chips wrap nicely to the next line instead of going off-screen
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableGroups.forEach { group ->
                    val isSelected = state.currentGroupId == group.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(DetailUiEvent.OnMoveToGroup(if (isSelected) null else group.id)) },
                        label = { Text(group.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VLColor.TrustGreen,
                            selectedLabelColor = VLColor.MidnightSlate,
                            containerColor = VLColor.SurfaceHigh,
                            labelColor = VLColor.TextPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            "Authentication Method",
            style = MaterialTheme.typography.labelMedium,
            color = VLColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Auth Type Selector!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val authOptions = listOf("TEXT_PASSWORD" to "Text Password", "PIN" to "Numeric PIN")

            authOptions.forEach { (backendValue, displayLabel) ->
                val isSelected = state.authType == backendValue
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onEvent(DetailUiEvent.OnAuthTypeSelected(backendValue)) },
                    label = { Text(displayLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VLColor.TrustGreen,
                        selectedLabelColor = VLColor.MidnightSlate,
                        containerColor = VLColor.SurfaceHigh,
                        labelColor = VLColor.TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val lockStatus = if (state.hasSecretSaved) "Update Password" else "Set Password"
        val lockColor = if (state.hasSecretSaved) VLColor.TrustGreen else VLColor.NudgeAmber
        Button(
            onClick = { viewModel.onEvent(DetailUiEvent.OnSetPasswordClicked) },
            colors = ButtonDefaults.buttonColors(containerColor = lockColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(lockStatus, color = VLColor.MidnightSlate)
        }
    }
}