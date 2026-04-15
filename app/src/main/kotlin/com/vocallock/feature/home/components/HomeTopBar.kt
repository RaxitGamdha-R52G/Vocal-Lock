package com.vocallock.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vocallock.core.ui.theme.VLColor

@Composable
fun HomeTopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Vocal-Lock",
                style = MaterialTheme.typography.headlineLarge,
                color = VLColor.TextPrimary
            )
            Text(
                text = "Your Voice, Your Vault",
                style = MaterialTheme.typography.bodyMedium,
                color = VLColor.TrustGreen
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                contentDescription = "Settings",
                tint = VLColor.TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}