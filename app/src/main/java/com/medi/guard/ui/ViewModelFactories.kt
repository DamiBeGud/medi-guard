package com.medi.guard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medi.guard.data.preferences.UserPreferencesRepository
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.ui.addmedication.AddMedicationViewModel
import com.medi.guard.ui.detail.MedicationDetailViewModel
import com.medi.guard.ui.history.HistoryViewModel
import com.medi.guard.ui.today.TodayViewModel

class MainViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(preferencesRepository) as T
    }
}

class TodayViewModelFactory(
    private val repository: MedicationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodayViewModel(repository) as T
    }
}

class AddMedicationViewModelFactory(
    private val repository: MedicationRepository,
    private val medicationId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddMedicationViewModel(repository, medicationId) as T
    }
}

class HistoryViewModelFactory(
    private val repository: MedicationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(repository) as T
    }
}

class MedicationDetailViewModelFactory(
    private val repository: MedicationRepository,
    private val medicationId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MedicationDetailViewModel(repository, medicationId) as T
    }
}
