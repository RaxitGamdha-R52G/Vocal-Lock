package com.vocallock.feature.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vocallock.R
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.data.provider.AppIconProvider
import org.koin.compose.koinInject

/**
 * A helper composable that asynchronously fetches the OS icon.
 */
@Composable
fun AsyncAppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider = koinInject()
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        imageBitmap = iconProvider.getAppIcon(packageName)
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier
        )
    } else {
        // Fallback placeholder while loading
        Box(
            modifier = modifier.background(VLColor.SurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                tint = VLColor.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}