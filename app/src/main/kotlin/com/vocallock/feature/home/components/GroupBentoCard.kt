package com.vocallock.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupBentoCard(
    group: GroupEntity,
    appsInGroup: List<AppEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxGridTiles: Int = 4,
    isHovered: Boolean = false
) {
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) VLColor.TrustGreen else Color.Transparent,
        label = "border_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) VLColor.TrustGreenBg else VLColor.SurfaceVariant,
        label = "bg_color"
    )

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = VLColor.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            val statusColor =
                if (group.encryptedSecret != null) VLColor.TrustGreen else VLColor.NudgeAmber
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(statusColor)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayApps = appsInGroup.take(maxGridTiles)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (displayApps.isEmpty()) {
                Text(
                    "No apps added",
                    style = MaterialTheme.typography.bodySmall,
                    color = VLColor.TextMuted
                )
            } else {
                displayApps.forEach { app ->
                    AsyncAppIcon(
                        packageName = app.packageName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}