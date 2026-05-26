package com.medi.guard.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.IntakeHistoryEntity
import com.medi.guard.data.room.IntakeStatus
import com.medi.guard.ui.UiFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryEntryUi(
    val id: Long,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledAtMillis: Long,
    val status: IntakeStatus,
    val statusText: String,
    val scheduledText: String,
    val takenText: String?
)

data class HistoryGroupUi(
    val label: String,
    val entries: List<HistoryEntryUi>
)

data class HistoryUiState(
    val query: String = "",
    val groups: List<HistoryGroupUi> = emptyList(),
    val weeklyText: String = "Noch keine bestätigten Einnahmen in dieser Woche."
)

class HistoryViewModel(
    repository: MedicationRepository
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.history,
        query
    ) { history, currentQuery ->
        val filtered = history.filter {
            currentQuery.isBlank() ||
                it.medicationName.contains(currentQuery, ignoreCase = true) ||
                it.dosage.contains(currentQuery, ignoreCase = true)
        }
        HistoryUiState(
            query = currentQuery,
            groups = filtered.grouped(),
            weeklyText = weeklySummary(history)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun updateQuery(value: String) {
        query.value = value
    }

    private fun List<IntakeHistoryEntity>.grouped(): List<HistoryGroupUi> {
        return map { it.toUi() }
            .groupBy { UiFormatters.historyDateLabel(it.scheduledAtMillis) }
            .map { (label, entries) -> HistoryGroupUi(label = label, entries = entries) }
    }

    private fun IntakeHistoryEntity.toUi(): HistoryEntryUi {
        return HistoryEntryUi(
            id = id,
            medicationId = medicationId,
            medicationName = medicationName,
            dosage = dosage,
            scheduledAtMillis = scheduledAtMillis,
            status = status,
            statusText = when (status) {
                IntakeStatus.TAKEN -> "Eingenommen"
                IntakeStatus.PENDING -> "Ausstehend"
                IntakeStatus.MISSED -> "Nicht bestätigt"
            },
            scheduledText = "Geplant: ${UiFormatters.timeWithUhr(scheduledAtMillis)}",
            takenText = takenAtMillis?.let { "Eingenommen um ${UiFormatters.timeWithUhr(it)}" }
        )
    }

    private fun weeklySummary(history: List<IntakeHistoryEntity>): String {
        val takenThisWeek = history.count {
            it.status == IntakeStatus.TAKEN &&
                it.takenAtMillis != null &&
                it.takenAtMillis >= System.currentTimeMillis() - 7 * 24 * 60 * 60_000L
        }
        return if (takenThisWeek == 0) {
            "Noch keine bestätigten Einnahmen in dieser Woche."
        } else {
            "Sie haben diese Woche $takenThisWeek Einnahme(n) bestätigt."
        }
    }
}
