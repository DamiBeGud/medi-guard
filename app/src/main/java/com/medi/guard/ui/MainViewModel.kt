package com.medi.guard.ui

import androidx.lifecycle.ViewModel
import com.medi.guard.data.preferences.UserPreferencesRepository

class MainViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val onboardingCompleted = preferencesRepository.onboardingCompleted

    fun completeOnboarding() {
        preferencesRepository.completeOnboarding()
    }
}
