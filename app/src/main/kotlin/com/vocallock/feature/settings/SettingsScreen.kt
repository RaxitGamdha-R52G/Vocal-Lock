package com.vocallock.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vocallock.BuildConfig
import com.vocallock.core.permission.UiPermissionEvent
import com.vocallock.core.ui.components.SectionHeader
import com.vocallock.core.ui.components.VLCard
import com.vocallock.core.ui.theme.VLColor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, permEvents: MutableSharedFlow<UiPermissionEvent>) {
    val vm: SettingsViewModel = koinViewModel()
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshStates(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = VLColor.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = VLColor.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VLColor.MidnightSlate),
            )
        },
        containerColor = VLColor.MidnightSlate,
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionHeader("Permissions")
            PermRow("Microphone", "Voice key recognition", s.hasAudio) {
                scope.launch { permEvents.emit(UiPermissionEvent.RequirePermission(Manifest.permission.RECORD_AUDIO)) }
            }
            PermRow("Notifications", "Password tips and alerts", s.hasNotifications) {
                scope.launch { permEvents.emit(UiPermissionEvent.RequirePermission(Manifest.permission.POST_NOTIFICATIONS)) }
            }
            PermRow("Accessibility", "Auto-type passwords into apps", s.accessibilityEnabled) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            PermRow("Display Over Apps", "Required to show the lock screen", s.isOverlayEnabled) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            }

            PermRow("Usage Access", "Detect foreground app launches", s.hasUsageAccess) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }

            PermRow(
                "Background Lock",
                "Keep watchdog alive in background",
                s.isIgnoringBatteryOptimizations
            ) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }

            SectionHeader("Visual Layout")
            VLCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Max Group Tiles",
                        color = VLColor.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = VLColor.TrustGreen.copy(0.15f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${s.maxGridTiles} tiles",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = VLColor.TrustGreen,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Slider(
                    value = s.maxGridTiles.toFloat(),
                    onValueChange = { vm.setMaxGridTiles(it.roundToInt()) },
                    valueRange = 2f..9f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = VLColor.TrustGreen,
                        activeTrackColor = VLColor.TrustGreen,
                        inactiveTrackColor = VLColor.Border
                    )
                )
                Text(
                    "Maximum number of app icons shown inside a Group Bento Card.",
                    color = VLColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SectionHeader("Password overuse reminder")
            VLCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Remind after",
                        color = VLColor.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = VLColor.TrustGreen.copy(0.15f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${s.nudgeThreshold} uses",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = VLColor.TrustGreen,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Slider(
                    value = s.nudgeThreshold.toFloat(),
                    onValueChange = { vm.setThreshold(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = VLColor.TrustGreen,
                        activeTrackColor = VLColor.TrustGreen,
                        inactiveTrackColor = VLColor.Border
                    )
                )
                Text(
                    "Advisory notification fires when a master password is used this many times per session.",
                    color = VLColor.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SectionHeader("About")
            VLCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Version" to BuildConfig.VERSION_NAME,
                        "Internet access" to "None — by design",
                        "Cloud backup" to "Disabled",
                        "Analytics" to "None",
                        "Account required" to "No"
                    ).forEach { (k, v) ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                k,
                                color = VLColor.TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                v,
                                color = VLColor.TextPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermRow(label: String, desc: String, granted: Boolean, onGrant: () -> Unit) {
    Surface(color = VLColor.Surface, shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    color = VLColor.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(desc, color = VLColor.TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            if (granted) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = VLColor.TrustGreen,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    color = VLColor.NudgeAmber.copy(0.12f),
                    shape = MaterialTheme.shapes.small,
                    onClick = onGrant
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = VLColor.NudgeAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Grant",
                            color = VLColor.NudgeAmber,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}