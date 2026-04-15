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
fun SetPasswordDialog(
    authType: String,
    onDismiss: () -> Unit,
    onPasswordConfirmed: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic UI based on what the user selected
    val isPin = authType == "PIN"
    val titleText = if (isPin) "Set Numeric PIN" else "Set Text Password"
    val labelText = if (isPin) "Enter PIN" else "Enter Password"
    val confirmLabelText = if (isPin) "Confirm PIN" else "Confirm Password"
    val keyboardType = if (isPin) KeyboardType.NumberPassword else KeyboardType.Password

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(VLColor.Surface)
                .padding(24.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                color = VLColor.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { input ->
                    if (isPin) {
                        if (input.all { it.isDigit() }) {
                            password = input
                            errorMessage = null
                        }
                    } else {
                        password = input
                        errorMessage = null
                    }
                },
                label = { Text(labelText) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = VLColor.TrustGreen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { input ->
                    if (isPin) {
                        if (input.all { it.isDigit() }) {
                            confirmPassword = input
                            errorMessage = null
                        }
                    } else {
                        confirmPassword = input
                        errorMessage = null
                    }
                },
                label = { Text(confirmLabelText) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VLColor.TextPrimary,
                    unfocusedTextColor = VLColor.TextPrimary,
                    focusedBorderColor = VLColor.TrustGreen,
                    errorBorderColor = VLColor.CrimsonRed
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage!!,
                    color = VLColor.CrimsonRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

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
                        if (password.isBlank()) {
                            errorMessage =
                                if (isPin) "PIN cannot be empty" else "Password cannot be empty"
                        } else if (password != confirmPassword) {
                            errorMessage =
                                if (isPin) "PINs do not match" else "Passwords do not match"
                        } else {
                            onPasswordConfirmed(password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VLColor.TrustGreen)
                ) {
                    Text("Save", color = VLColor.MidnightSlate)
                }
            }
        }
    }
}