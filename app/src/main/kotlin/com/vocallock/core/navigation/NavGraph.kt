package com.vocallock.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vocallock.core.permission.PermissionHandlerComposable
import com.vocallock.core.permission.UiPermissionEvent
import com.vocallock.feature.detail.DetailScreen
import com.vocallock.feature.group.GroupContentsScreen
import com.vocallock.feature.home.HomeScreen
import com.vocallock.feature.selector.AppSelectorScreen
import com.vocallock.feature.settings.SettingsScreen
import com.vocallock.feature.splash.SplashScreen
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun VocalLockNavGraph(navController: NavHostController) {
    val permEvents = remember { MutableSharedFlow<UiPermissionEvent>(extraBufferCapacity = 1) }

    PermissionHandlerComposable(events = permEvents) {
        NavHost(navController = navController, startDestination = "splash") {

            composable("splash") {
                SplashScreen(
                    onFinished = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToDetail = { targetId, isGroup ->
                        if (isGroup) {
                            navController.navigate("group_contents/$targetId")
                        } else {
                            navController.navigate("detail/$targetId/false")
                        }
                    },
                    onNavigateToAppSelector = { navController.navigate("app_selector") },
                    permEvents = permEvents
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    permEvents = permEvents
                )
            }

            composable(
                route = "group_contents/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) {
                GroupContentsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGroupSettings = { groupId ->
                        navController.navigate("detail/$groupId/true")
                    },
                    onNavigateToAppDetail = { pkg ->
                        navController.navigate("detail/$pkg/false")
                    },
                    onNavigateToAddApps = { groupId ->
                        navController.navigate("app_selector?groupId=$groupId")
                    }
                )
            }

            composable(
                route = "detail/{targetId}/{isGroup}",
                arguments = listOf(
                    navArgument("targetId") { type = NavType.StringType },
                    navArgument("isGroup") { type = NavType.BoolType }
                )
            ) {
                DetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    permEvents = permEvents
                )
            }

            composable(
                route = "app_selector?groupId={groupId}",
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                AppSelectorScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}