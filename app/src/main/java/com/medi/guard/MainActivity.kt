package com.medi.guard

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medi.guard.ui.MainViewModel
import com.medi.guard.ui.MainViewModelFactory
import com.medi.guard.ui.navigation.MediGuardMainApp
import com.medi.guard.ui.onboarding.OnboardingScreen
import com.medi.guard.ui.theme.MediGuardTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // The app still works without notification permission, but reminders cannot be displayed.
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MediGuardApplication).userPreferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsStateWithLifecycle()
            MediGuardTheme {
                if (onboardingCompleted) {
                    MediGuardMainApp(app = application as MediGuardApplication)
                } else {
                    OnboardingScreen(onStartClick = mainViewModel::completeOnboarding)
                }
            }
        }
    }

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
}
