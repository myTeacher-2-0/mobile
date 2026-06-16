package com.crw.myteacher.push

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Manager uprawnień powiadomień i cyklu życia serwisu.
 *
 * Użycie w Activity:
 * ```
 * val pushManager = PushPermissionManager(this)
 * pushManager.requestPermissionAndStartService()
 * ```
 */
class PushPermissionManager(private val activity: ComponentActivity) {

    companion object {
        private const val TAG = "PushPermissionManager"
    }

    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Uprawnienie POST_NOTIFICATIONS przyznane")
                startService()
            } else {
                Log.w(TAG, "Uprawnienie POST_NOTIFICATIONS odmówione")
                // Serwis i tak może działać, ale nie będzie wyświetlał powiadomień
                startService()
            }
        }

    /**
     * Sprawdza uprawnienia i uruchamia serwis powiadomień.
     * Na Android 13+ (API 33) pyta o uprawnienie POST_NOTIFICATIONS.
     */
    fun requestPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "POST_NOTIFICATIONS już przyznane")
                    startService()
                }
                else -> {
                    Log.d(TAG, "Pytam o POST_NOTIFICATIONS")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android < 13 nie wymaga runtime permission dla powiadomień
            startService()
        }
    }

    private fun startService() {
        PushNotificationService.start(activity)
    }

    /**
     * Sprawdza czy uprawnienia do powiadomień są przyznane.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

