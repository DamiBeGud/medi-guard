package com.medi.guard

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medi.guard.ui.MainViewModel
import com.medi.guard.ui.MainViewModelFactory
import com.medi.guard.ui.navigation.MediGuardMainApp
import com.medi.guard.ui.onboarding.OnboardingScreen
import com.medi.guard.ui.theme.MediGuardTheme

class MainActivity : ComponentActivity() {
    private var exactAlarmAccessGranted by mutableStateOf(true)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // The app still works without notification permission, but reminders cannot be displayed.
    }

    private val exactAlarmAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshExactAlarmAccessState()
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MediGuardApplication).userPreferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep permission-related UI state current before the first Compose frame is built.
        refreshExactAlarmAccessState()
        requestNotificationPermissionIfNeeded()

        setContent {
            val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsStateWithLifecycle()
            MediGuardTheme {
                if (onboardingCompleted) {
                    MediGuardMainApp(app = application as MediGuardApplication)
                } else {
                    OnboardingScreen(
                        onStartClick = mainViewModel::completeOnboarding,
                        exactAlarmAccessGranted = exactAlarmAccessGranted,
                        onRequestExactAlarmAccessClick = ::requestExactAlarmAccessIfNeeded
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check exact alarm access after returning from system settings.
        refreshExactAlarmAccessState()
    }

    // Requests Android 13+ notification permission so reminders can be shown.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Opens the system screen where the user can grant the exact-alarm special access.
    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == true) {
            exactAlarmAccessGranted = true
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        exactAlarmAccessLauncher.launch(intent)
    }

    // Mirrors the current exact-alarm access into Compose state for onboarding and scheduling UI.
    private fun refreshExactAlarmAccessState() {
        exactAlarmAccessGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == true
        }
    }
}
