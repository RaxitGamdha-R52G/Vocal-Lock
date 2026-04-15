package com.vocallock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.vocallock.core.navigation.VocalLockNavGraph
import com.vocallock.core.permission.PermissionManager
import com.vocallock.core.ui.theme.VocalLockTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val permissionManager: PermissionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            VocalLockTheme {
                val navController = rememberNavController()
                VocalLockNavGraph(navController = navController)
            }
        }
    }
}