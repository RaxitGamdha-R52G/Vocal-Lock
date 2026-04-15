package com.vocallock.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vocallock.core.ui.theme.VLColor

@Composable
fun DeleteConfirmationDialog(
    isGroup: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VLColor.Surface)
                .padding(24.dp)
        ) {
            Text(
                text = if (isGroup) "Delete Group?" else "Remove from Vault?",
                style = MaterialTheme.typography.titleLarge,
                color = VLColor.CrimsonRed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isGroup) {
                    "Are you sure you want to delete this group? The apps inside it will become standalone apps in your vault."
                } else {
                    "Are you sure you want to remove this app? It will no longer be protected by Vocal-Lock."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = VLColor.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VLColor.TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = VLColor.CrimsonRed)
                ) {
                    Text("Delete", color = VLColor.MidnightSlate)
                }
            }
        }
    }
}