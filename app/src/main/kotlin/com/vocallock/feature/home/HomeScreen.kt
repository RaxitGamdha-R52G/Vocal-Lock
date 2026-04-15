package com.vocallock.feature.home

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vocallock.core.permission.PermissionManager
import com.vocallock.core.permission.PermissionStatus
import com.vocallock.core.permission.UiPermissionEvent
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity
import com.vocallock.feature.home.components.AppBentoCard
import com.vocallock.feature.home.components.CreateGroupDialog
import com.vocallock.feature.home.components.GroupBentoCard
import com.vocallock.feature.home.components.HomeTopBar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (targetId: String, isGroup: Boolean) -> Unit,
    onNavigateToAppSelector: () -> Unit,
    permEvents: MutableSharedFlow<UiPermissionEvent>
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionManager: PermissionManager = koinInject()
    val scope = rememberCoroutineScope()

    val permStates by permissionManager.states.collectAsState()
    val hasAudio = permStates[Manifest.permission.RECORD_AUDIO] == PermissionStatus.GRANTED
    val hasNotifications =
        permStates[Manifest.permission.POST_NOTIFICATIONS] == PermissionStatus.GRANTED

    var hasAccessibility by remember { mutableStateOf(true) }
    var hasOverlay by remember { mutableStateOf(true) }
    var hasUsageAccess by remember { mutableStateOf(true) }
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(true) }

    var isFabExpanded by remember { mutableStateOf(false) }

    // ── DRAG AND DROP STATE ──────────────────────────────────────────
    var isDragging by remember { mutableStateOf(false) }
    var draggedApp by remember { mutableStateOf<AppEntity?>(null) }
    var draggedGroup by remember { mutableStateOf<GroupEntity?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Intersection Math Memory
    val groupBounds = remember { mutableStateMapOf<String, Rect>() }
    var trashBounds by remember { mutableStateOf(Rect.Zero) }

    // Hover States
    var hoveredGroupId by remember { mutableStateOf<String?>(null) }
    var isHoveringTrash by remember { mutableStateOf(false) }

    val listScale by animateFloatAsState(if (isDragging) 0.95f else 1f, label = "list_scale")

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = permissionManager.isAccessibilityEnabled()
                hasOverlay = Settings.canDrawOverlays(context)
                hasUsageAccess = permissionManager.hasUsageAccess()
                isIgnoringBatteryOptimizations = permissionManager.isIgnoringBatteryOptimizations()
                permissionManager.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            if (effect is HomeUiEffect.NavigateToDetail) {
                onNavigateToDetail(effect.targetId, effect.isGroup)
            }
        }
    }

    if (state.showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { viewModel.onEvent(HomeUiEvent.OnDismissGroupDialog) },
            onConfirm = { name, pass ->
                viewModel.onEvent(
                    HomeUiEvent.OnConfirmCreateGroup(
                        name,
                        pass
                    )
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = isFabExpanded && !isDragging,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 50 }) + fadeOut()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Lock App",
                                color = VLColor.TextPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onNavigateToAppSelector()
                                },
                                containerColor = VLColor.SurfaceHigh,
                                contentColor = VLColor.NeonCyan
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock New App")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = isFabExpanded && !isDragging,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { 50 }) + fadeOut()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "New Group",
                                color = VLColor.TextPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    viewModel.onEvent(HomeUiEvent.OnCreateGroupClicked)
                                },
                                containerColor = VLColor.SurfaceHigh,
                                contentColor = VLColor.TrustGreen
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = "New Group")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = !isDragging, enter = fadeIn(), exit = fadeOut()) {
                        val rotation by animateFloatAsState(
                            if (isFabExpanded) 45f else 0f,
                            label = "fab_rotation"
                        )
                        FloatingActionButton(
                            onClick = { isFabExpanded = !isFabExpanded },
                            containerColor = if (isFabExpanded) VLColor.SurfaceHigh else VLColor.TrustGreen,
                            contentColor = if (isFabExpanded) VLColor.TextPrimary else VLColor.MidnightSlate
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Expand Menu",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }
                }
            },
            containerColor = VLColor.MidnightSlate
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(VLColor.MidnightSlate)
                ) {
                    HomeTopBar(onSettingsClick = onNavigateToSettings)

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VLColor.TrustGreen)
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(listScale),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalItemSpacing = 16.dp
                        ) {
                            // ── GROUPS ──
                            state.groupsWithApps.forEach { (group, apps) ->
                                item(key = "group_${group.id}") {
                                    val isBeingDragged = draggedGroup?.id == group.id
                                    val itemAlpha by animateFloatAsState(
                                        if (isBeingDragged) 0f else 1f,
                                        label = "alpha"
                                    )

                                    var itemGlobalOffset by remember { mutableStateOf(Offset.Zero) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .zIndex(if (isBeingDragged) 10f else 0f)
                                            .onGloballyPositioned { coords ->
                                                groupBounds[group.id] = coords.boundsInWindow()
                                                itemGlobalOffset = coords.localToWindow(Offset.Zero)
                                            }
                                            .pointerInput(group) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        isDragging = true
                                                        draggedGroup = group
                                                        dragOffset = Offset.Zero
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffset += dragAmount

                                                        // Calculate absolute center of the dragged item
                                                        val currentGlobalCenter =
                                                            itemGlobalOffset + dragOffset + Offset(
                                                                size.width / 2f,
                                                                size.height / 2f
                                                            )
                                                        isHoveringTrash =
                                                            trashBounds.contains(currentGlobalCenter)
                                                    },
                                                    onDragEnd = {
                                                        if (isHoveringTrash) {
                                                            viewModel.onEvent(
                                                                HomeUiEvent.OnDeleteGroup(
                                                                    group.id
                                                                )
                                                            )
                                                        }
                                                        isDragging = false
                                                        draggedGroup = null
                                                        isHoveringTrash = false
                                                    },
                                                    onDragCancel = {
                                                        isDragging = false
                                                        draggedGroup = null
                                                        isHoveringTrash = false
                                                    }
                                                )
                                            }
                                    ) {
                                        GroupBentoCard(
                                            group = group,
                                            appsInGroup = apps,
                                            onClick = {
                                                if (!isDragging) viewModel.onEvent(
                                                    HomeUiEvent.OnGroupClicked(group.id)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(alpha = itemAlpha),
                                            isHovered = hoveredGroupId == group.id // Highlight if an app is dragged over it!
                                        )

                                        // Ghost Item that follows your finger
                                        if (isBeingDragged) {
                                            val offsetX by animateDpAsState(
                                                dragOffset.x.dp,
                                                label = "x"
                                            )
                                            val offsetY by animateDpAsState(
                                                dragOffset.y.dp,
                                                label = "y"
                                            )

                                            GroupBentoCard(
                                                group = group,
                                                appsInGroup = apps,
                                                onClick = {},
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .offset {
                                                        IntOffset(
                                                            offsetX.roundToPx(),
                                                            offsetY.roundToPx()
                                                        )
                                                    }
                                                    .scale(1.05f)
                                                    .zIndex(10f)
                                            )
                                        }
                                    }
                                }
                            }

                            // ── STANDALONE APPS ──
                            items(
                                items = state.standaloneApps,
                                key = { app -> "app_${app.packageName}" }
                            ) { app ->
                                val isBeingDragged = draggedApp?.packageName == app.packageName
                                val itemAlpha by animateFloatAsState(
                                    if (isBeingDragged) 0f else 1f,
                                    label = "alpha"
                                )

                                var itemGlobalOffset by remember { mutableStateOf(Offset.Zero) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(if (isBeingDragged) 10f else 0f)
                                        .onGloballyPositioned { coords ->
                                            itemGlobalOffset = coords.localToWindow(Offset.Zero)
                                        }
                                        .pointerInput(app) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    isDragging = true
                                                    draggedApp = app
                                                    dragOffset = Offset.Zero
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset += dragAmount

                                                    // Calculate absolute center to detect intersections!
                                                    val currentGlobalCenter =
                                                        itemGlobalOffset + dragOffset + Offset(
                                                            size.width / 2f,
                                                            size.height / 2f
                                                        )

                                                    // Math: Does the finger intersect any group or the trash?
                                                    isHoveringTrash =
                                                        trashBounds.contains(currentGlobalCenter)
                                                    hoveredGroupId = groupBounds.entries.find {
                                                        it.value.contains(currentGlobalCenter)
                                                    }?.key
                                                },
                                                onDragEnd = {
                                                    if (isHoveringTrash) {
                                                        viewModel.onEvent(
                                                            HomeUiEvent.OnDeleteAppLock(
                                                                app.packageName
                                                            )
                                                        )
                                                    } else if (hoveredGroupId != null) {
                                                        // SUCCESS! Drop app into group!
                                                        viewModel.onEvent(
                                                            HomeUiEvent.OnMoveAppToGroup(
                                                                app.packageName,
                                                                hoveredGroupId
                                                            )
                                                        )
                                                    }
                                                    isDragging = false
                                                    draggedApp = null
                                                    isHoveringTrash = false
                                                    hoveredGroupId = null
                                                },
                                                onDragCancel = {
                                                    isDragging = false
                                                    draggedApp = null
                                                    isHoveringTrash = false
                                                    hoveredGroupId = null
                                                }
                                            )
                                        }
                                ) {
                                    AppBentoCard(
                                        app = app,
                                        onClick = {
                                            if (!isDragging) viewModel.onEvent(
                                                HomeUiEvent.OnAppClicked(
                                                    app.packageName
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(alpha = itemAlpha)
                                    )

                                    if (isBeingDragged) {
                                        val offsetX by animateDpAsState(
                                            dragOffset.x.dp,
                                            label = "x"
                                        )
                                        val offsetY by animateDpAsState(
                                            dragOffset.y.dp,
                                            label = "y"
                                        )

                                        AppBentoCard(
                                            app = app,
                                            onClick = {},
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset {
                                                    IntOffset(
                                                        offsetX.roundToPx(),
                                                        offsetY.roundToPx()
                                                    )
                                                }
                                                .scale(1.05f)
                                                .zIndex(10f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── THE TRASH BUCKET ──
                AnimatedVisibility(
                    visible = isDragging,
                    enter = slideInVertically(initialOffsetY = { -200 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -200 }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .zIndex(5f)
                ) {
                    val bucketColor =
                        if (isHoveringTrash) VLColor.RubyRed else VLColor.CrimsonRed.copy(alpha = 0.8f)
                    val bucketScale by animateFloatAsState(
                        if (isHoveringTrash) 1.1f else 1f,
                        label = "bucket_scale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .scale(bucketScale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(bucketColor)
                            .onGloballyPositioned { coords ->
                                trashBounds = coords.boundsInWindow()
                            }
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = VLColor.TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isHoveringTrash) "Release to Delete" else "Drag here to Remove from Vault",
                            color = VLColor.TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (isFabExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VLColor.MidnightSlate.copy(alpha = 0.6f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { isFabExpanded = false }
                    )
                }
            }
        }

        // --- Permission Wall ---
        val showWall =
            !hasAccessibility || !hasOverlay || !hasAudio || !hasNotifications || !hasUsageAccess || !isIgnoringBatteryOptimizations
        AnimatedVisibility(
            visible = showWall,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VLColor.MidnightSlate.copy(alpha = 0.95f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(VLColor.Surface)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Ultimate Setup Required",
                        style = MaterialTheme.typography.headlineMedium,
                        color = VLColor.CrimsonSoft
                    )
                    Text(
                        "Vocal-Lock requires these core system permissions to build a bulletproof vault.",
                        color = VLColor.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                permEvents.emit(
                                    UiPermissionEvent.RequirePermission(
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasAudio) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (hasAudio) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasAudio) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (hasAudio) "Microphone Granted" else "Grant Microphone",
                            color = if (hasAudio) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasOverlay) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (hasOverlay) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasOverlay) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (hasOverlay) "Overlay Granted" else "Grant Display Over Apps",
                            color = if (hasOverlay) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasUsageAccess) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (hasUsageAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasUsageAccess) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (hasUsageAccess) "Usage Access Granted" else "Grant Usage Access",
                            color = if (hasUsageAccess) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasAccessibility) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (hasAccessibility) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasAccessibility) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (hasAccessibility) "Accessibility Granted" else "Grant Accessibility",
                            color = if (hasAccessibility) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Button(
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = "package:${context.packageName}".toUri()
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIgnoringBatteryOptimizations) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (isIgnoringBatteryOptimizations) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isIgnoringBatteryOptimizations) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isIgnoringBatteryOptimizations) "Background Lock Active" else "Allow Background Lock",
                            color = if (isIgnoringBatteryOptimizations) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                permEvents.emit(
                                    UiPermissionEvent.RequirePermission(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasNotifications) VLColor.SurfaceHigh else VLColor.TrustGreen)
                    ) {
                        Icon(
                            imageVector = if (hasNotifications) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasNotifications) VLColor.TrustGreen else VLColor.MidnightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (hasNotifications) "Notifications Granted" else "Grant Notifications",
                            color = if (hasNotifications) VLColor.TextSecondary else VLColor.MidnightSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

fun Modifier.padding(alpha: Float) = this.background(VLColor.MidnightSlate.copy(alpha = 1f - alpha))