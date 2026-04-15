package com.vocallock.feature.group

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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.vocallock.core.ui.theme.VLColor
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.feature.home.components.AppBentoCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupContentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGroupSettings: (String) -> Unit,
    onNavigateToAppDetail: (String) -> Unit,
    onNavigateToAddApps: (String) -> Unit,
    viewModel: GroupContentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    var isEditingName by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var isDragging by remember { mutableStateOf(false) }
    var draggedApp by remember { mutableStateOf<AppEntity?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isHoveringDelete by remember { mutableStateOf(false) }

    val listScale by animateFloatAsState(if (isDragging) 0.92f else 1f, label = "list_scale")
    val listAlpha by animateFloatAsState(if (isDragging) 0.6f else 1f, label = "list_alpha")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (isEditingName) {
                        isEditingName = false
                        focusManager.clearFocus()
                        if (editNameText.isNotBlank()) viewModel.renameGroup(editNameText.trim())
                    }
                })
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (isEditingName) {
                            BasicTextField(
                                value = editNameText,
                                onValueChange = { editNameText = it },
                                textStyle = MaterialTheme.typography.titleLarge.copy(color = VLColor.NeonCyan),
                                cursorBrush = SolidColor(VLColor.NeonCyan),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    isEditingName = false
                                    focusManager.clearFocus()
                                    if (editNameText.isNotBlank()) viewModel.renameGroup(
                                        editNameText.trim()
                                    )
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    editNameText = state.group?.name ?: ""
                                    isEditingName = true
                                }
                            ) {
                                Text(state.group?.name ?: "Group", color = VLColor.TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = VLColor.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = VLColor.TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { state.group?.let { onNavigateToGroupSettings(it.id) } }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Group Settings",
                                tint = VLColor.TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = VLColor.MidnightSlate)
                )
            },
            floatingActionButton = {
                AnimatedVisibility(visible = !isDragging, enter = fadeIn(), exit = fadeOut()) {
                    FloatingActionButton(
                        onClick = { onNavigateToAddApps(viewModel.groupId) },
                        containerColor = VLColor.TrustGreen,
                        contentColor = VLColor.MidnightSlate
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Apps")
                    }
                }
            },
            containerColor = VLColor.MidnightSlate
        ) { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VLColor.TrustGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(listScale)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.apps.isEmpty()) {
                            item {
                                Text(
                                    "No apps in this group yet. Click the + button to add some!",
                                    color = VLColor.TextMuted
                                )
                            }
                        } else {
                            itemsIndexed(
                                state.apps,
                                key = { _, app -> app.packageName }) { index, app ->

                                val isBeingDragged = draggedApp?.packageName == app.packageName
                                val itemAlpha by animateFloatAsState(
                                    if (isBeingDragged) 0f else listAlpha,
                                    label = "item_alpha"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(if (isBeingDragged) 1f else 0f)
                                        .pointerInput(app) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    isDragging = true
                                                    draggedApp = app
                                                    dragOffset = Offset(0f, offset.y - 100f)
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset += dragAmount

                                                    isHoveringDelete = dragOffset.y < -150f

                                                    if (!isHoveringDelete) {
                                                        if (dragAmount.y > 60f && index < state.apps.size - 1) {
                                                            viewModel.reorderAppsLocally(
                                                                index,
                                                                index + 1
                                                            )
                                                            dragOffset = Offset.Zero
                                                        } else if (dragAmount.y < -60f && index > 0) {
                                                            viewModel.reorderAppsLocally(
                                                                index,
                                                                index - 1
                                                            )
                                                            dragOffset = Offset.Zero
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    if (isHoveringDelete) {
                                                        viewModel.removeAppFromGroup(app)
                                                    } else {
                                                        viewModel.saveAppOrderToDatabase()
                                                    }
                                                    isDragging = false
                                                    draggedApp = null
                                                    isHoveringDelete = false
                                                },
                                                onDragCancel = {
                                                    isDragging = false
                                                    draggedApp = null
                                                    isHoveringDelete = false
                                                }
                                            )
                                        }
                                ) {
                                    AppBentoCard(
                                        app = app,
                                        onClick = { if (!isDragging) onNavigateToAppDetail(app.packageName) },
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
                                                .background(
                                                    VLColor.SurfaceHigh,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .zIndex(10f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isDragging,
                    enter = slideInVertically(initialOffsetY = { -200 }),
                    exit = slideOutVertically(targetOffsetY = { -200 }),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .zIndex(5f)
                ) {
                    val bucketColor =
                        if (isHoveringDelete) VLColor.RubyRed else VLColor.CrimsonRed.copy(alpha = 0.8f)
                    val bucketScale by animateFloatAsState(
                        if (isHoveringDelete) 1.1f else 1f,
                        label = "bucket_scale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .scale(bucketScale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(bucketColor)
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
                            if (isHoveringDelete) "Release to Remove" else "Drag here to remove from Group",
                            color = VLColor.TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.padding(alpha: Float) = this.background(VLColor.MidnightSlate.copy(alpha = 1f - alpha))