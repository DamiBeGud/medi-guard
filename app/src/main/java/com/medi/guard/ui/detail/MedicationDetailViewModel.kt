package com.medi.guard.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.IntakeHistoryEntity
import com.medi.guard.data.room.MedicationEntity
import com.medi.guard.ui.UiFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MedicationDetailUiState(
    val medication: MedicationEntity? = null,
    val reminderTime: String = "",
    val lastTakenText: String = "Noch keine Einnahme bestätigt",
    val message: String? = null,
    val deleted: Boolean = false
)

class MedicationDetailViewModel(
    private val repository: MedicationRepository,
    private val medicationId: Long
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    private val deleted = MutableStateFlow(false)

    val uiState: StateFlow<MedicationDetailUiState> = combine(
        repository.observeMedication(medicationId),
        repository.observeHistoryForMedication(medicationId),
        message,
        deleted
    ) { medication, history, currentMessage, isDeleted ->
        MedicationDetailUiState(
            medication = medication,
            reminderTime = medication?.let {
                UiFormatters.timeWithUhr(it.reminderHour, it.reminderMinute)
            }.orEmpty(),
            lastTakenText = lastTakenText(history),
            message = currentMessage,
            deleted = isDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MedicationDetailUiState()
    )

    fun markTaken() {
        viewModelScope.launch {
            val medication = repository.getMedication(medicationId) ?: return@launch
            val scheduledAt = MedicationRepository.scheduledTimeForToday(
                medication.reminderHour,
                medication.reminderMinute
            )
            if (repository.markMedicationTaken(medicationId, scheduledAt)) {
                message.value = "Einnahme gespeichert"
            }
        }
    }

    fun snooze() {
        val medication = uiState.value.medication ?: return
        val scheduledAt = MedicationRepository.scheduledTimeForToday(
            medication.reminderHour,
            medication.reminderMinute
        )
        repository.snoozeMedication(medicationId, scheduledAt)
        message.value = "Erinnerung in 15 Minuten"
    }

    fun togglePaused() {
        viewModelScope.launch {
            val medication = repository.getMedication(medicationId) ?: return@launch
            val paused = medication.isActive
            if (repository.setMedicationPaused(medicationId, paused)) {
                message.value = if (paused) "Erinnerung pausiert" else "Erinnerung aktiv"
            }
        }
    }

    fun deleteMedication() {
        viewModelScope.launch {
            if (repository.deleteMedication(medicationId)) {
                deleted.value = true
            }
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun lastTakenText(history: List<IntakeHistoryEntity>): String {
        val lastTaken = history.firstOrNull { it.takenAtMillis != null } ?: return "Noch keine Einnahme bestätigt"
        val takenAtMillis = lastTaken.takenAtMillis ?: return "Noch keine Einnahme bestätigt"
        return "Zuletzt eingenommen um ${UiFormatters.timeWithUhr(takenAtMillis)}"
    }
}
