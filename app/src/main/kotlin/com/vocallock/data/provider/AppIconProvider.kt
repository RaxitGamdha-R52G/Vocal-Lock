package com.vocallock.data.provider

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppIconProvider(private val context: Context) {
    val packageManager: PackageManager = context.packageManager

    /**
     * Lists all non-system apps installed on the device.
     */
    fun getInstalledApps(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter {
            // Filter out system apps to keep the list clean
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }
    }

    suspend fun getAppIcon(packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            val drawable = packageManager.getApplicationIcon(packageName)
            drawableToImageBitmap(drawable)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}