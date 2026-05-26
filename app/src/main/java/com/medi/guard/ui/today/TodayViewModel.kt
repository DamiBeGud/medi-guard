package com.medi.guard.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.IntakeHistoryEntity
import com.medi.guard.data.room.IntakeStatus
import com.medi.guard.data.room.MedicationEntity
import com.medi.guard.ui.UiFormatters
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReminderStatus {
    PENDING,
    TAKEN,
    MISSED
}

data class TodayMedicationUi(
    val id: Long,
    val name: String,
    val dosage: String,
    val time: String,
    val scheduledAtMillis: Long,
    val status: ReminderStatus,
    val isActive: Boolean
)

data class TodayUiState(
    val reminders: List<TodayMedicationUi> = emptyList(),
    val message: String? = null
)

class TodayViewModel(
    private val repository: MedicationRepository
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TodayUiState> = combine(
        repository.activeMedications,
        repository.history,
        message
    ) { medications, history, currentMessage ->
        TodayUiState(
            reminders = medications.map { medication ->
                medication.toTodayMedicationUi(history)
            },
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState()
    )

    fun markTaken(medicationId: Long, scheduledAtMillis: Long) {
        viewModelScope.launch {
            if (repository.markMedicationTaken(medicationId, scheduledAtMillis)) {
                message.value = "Einnahme gespeichert"
            }
        }
    }

    fun snooze(medicationId: Long, scheduledAtMillis: Long) {
        repository.snoozeMedication(medicationId, scheduledAtMillis)
        message.value = "Erinnerung in 15 Minuten"
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun MedicationEntity.toTodayMedicationUi(history: List<IntakeHistoryEntity>): TodayMedicationUi {
        val scheduledAt = MedicationRepository.scheduledTimeForToday(reminderHour, reminderMinute)
        val status = statusForMedication(id, scheduledAt, history)
        return TodayMedicationUi(
            id = id,
            name = name,
            dosage = dosage,
            time = UiFormatters.time(reminderHour, reminderMinute),
            scheduledAtMillis = scheduledAt,
            status = status,
            isActive = isActive
        )
    }

    private fun statusForMedication(
        medicationId: Long,
        scheduledAtMillis: Long,
        history: List<IntakeHistoryEntity>
    ): ReminderStatus {
        val startOfDay = MedicationRepository.startOfTodayMillis()
        val endOfDay = MedicationRepository.endOfTodayMillis()
        val takenToday = history.any {
            it.medicationId == medicationId &&
                it.status == IntakeStatus.TAKEN &&
                it.scheduledAtMillis in startOfDay..endOfDay
        }
        if (takenToday) return ReminderStatus.TAKEN

        val missedAfterMillis = scheduledAtMillis + 60 * 60_000L
        return if (Calendar.getInstance().timeInMillis > missedAfterMillis) {
            ReminderStatus.MISSED
        } else {
            ReminderStatus.PENDING
        }
    }
}
