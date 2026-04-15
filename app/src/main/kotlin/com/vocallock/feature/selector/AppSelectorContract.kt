package com.vocallock.feature.selector

import android.graphics.drawable.Drawable

/**
 * Represents an app installed on the device, ready to be selected.
 */
data class SelectableApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isSelected: Boolean = false
)

data class AppSelectorUiState(
    val isLoading: Boolean = true,
    val apps: List<SelectableApp> = emptyList(),
    val searchQuery: String = ""
)

sealed class AppSelectorUiEvent {
    data class OnSearchChanged(val query: String) : AppSelectorUiEvent()
    data class OnAppToggled(val packageName: String) : AppSelectorUiEvent()
    object OnConfirmSelection : AppSelectorUiEvent()
    object OnBackClicked : AppSelectorUiEvent()
}