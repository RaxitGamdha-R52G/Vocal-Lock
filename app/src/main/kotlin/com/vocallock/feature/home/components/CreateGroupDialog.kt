package com.vocallock.feature.home.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vocallock.core.ui.theme.VLColor

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var masterPassword by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(VLColor.Surface)
                .padding(24.dp)
        ) {
            Text(
                text = "Create Security Vault",
                style = MaterialTheme.typography.titleLarge,
                color = VLColor.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = {
                    groupName = it
                    isError = false
                },
                label = { Text("Group Name (e.g. Finance)") },
                singleLine = true,
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = VLColor.TrustGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (isError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Name cannot be empty",
                    color = VLColor.CrimsonRed,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Apps added to this group will inherit this password unless you give them a specific one later.",
                color = VLColor.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = masterPassword,
                onValueChange = { masterPassword = it },
                label = { Text("Master Password (Optional)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = VLColor.TrustGreen
                ),
                modifier = Modifier.fillMaxWidth()
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
                    onClick = {
                        if (groupName.isBlank()) {
                            isError = true
                        } else {
                            val finalPassword = masterPassword.takeIf { it.isNotBlank() }
                            onConfirm(groupName.trim(), finalPassword)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VLColor.TrustGreen)
                ) {
                    Text("Create", color = VLColor.MidnightSlate)
                }
            }
        }
    }
}