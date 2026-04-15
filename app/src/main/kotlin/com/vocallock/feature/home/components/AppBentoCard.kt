package com.vocallock.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.data.database.entity.AppEntity

@Composable
fun AppBentoCard(
    app: AppEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(VLColor.Surface)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncAppIcon(
            packageName = app.packageName,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = app.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = VLColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val statusColor =
            if (app.encryptedSecretOverride != null) VLColor.TrustGreen else VLColor.NudgeAmber
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(MaterialTheme.shapes.small)
                .background(statusColor)
        )
    }
}