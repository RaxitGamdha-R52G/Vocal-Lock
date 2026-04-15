package com.vocallock.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocallock.core.ui.theme.VLColor

// ── VLCard ────────────────────────────────────────────────────

@Composable
fun VLCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape =
        RoundedCornerShape(topStart = 16.dp, topEnd = 6.dp, bottomEnd = 16.dp, bottomStart = 6.dp)
    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = VLColor.Surface,
            onClick = onClick,
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = VLColor.Surface) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

// ── SectionHeader ─────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = VLColor.TextSecondary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

// ── PrivacyPill (Used in Splash Screen) ───────────────────────

@Composable
fun PrivacyPill(text: String, isTeal: Boolean, modifier: Modifier = Modifier) {
    val bg =
        if (isTeal) VLColor.TrustGreenBg.copy(alpha = 0.8f) else Color(0xFF1A1530).copy(alpha = 0.9f)
    val dot = if (isTeal) VLColor.TrustGreen else Color(0xFF7F77DD)
    val txtC = if (isTeal) VLColor.TrustGreenSoft else Color(0xFFCECBF6)
    val strokeC = if (isTeal) VLColor.TrustGreenDim else Color(0xFF534AB7)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(0.5.dp, strokeC.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(
            Modifier
                .size(7.dp)
                .padding(top = 4.dp)
        ) { drawCircle(dot) }
        Text(text, style = MaterialTheme.typography.bodySmall, color = txtC, lineHeight = 19.sp)
    }
}