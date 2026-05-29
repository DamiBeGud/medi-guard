package com.medi.guard.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.IntakeHistoryEntity
import com.medi.guard.data.room.IntakeStatus
import com.medi.guard.data.room.MedicationEntity
import com.medi.guard.data.room.RepeatOption
import com.medi.guard.ui.UiFormatters
import java.util.Calendar
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

data class WeeklyOverviewBarUi(
    val label: String,
    val dueCount: Int,
    val takenCount: Int,
    val isToday: Boolean
)

data class HistoryUiState(
    val groups: List<HistoryGroupUi> = emptyList(),
    val weeklyBars: List<WeeklyOverviewBarUi> = emptyList(),
    val weeklyText: String = "Noch keine bestätigten Einnahmen in dieser Woche."
)

class HistoryViewModel(
    repository: MedicationRepository
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = combine(
        repository.history,
        repository.activeMedications
    ) { history, medications ->
        val weeklyBars = weeklyBars(medications, history)
        HistoryUiState(
            groups = history.grouped(),
            weeklyBars = weeklyBars,
            weeklyText = weeklySummary(weeklyBars)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    // Groups raw history rows by their visible calendar date for sectioned rendering in the timeline.
    private fun List<IntakeHistoryEntity>.grouped(): List<HistoryGroupUi> {
        return map { it.toUi() }
            .groupBy { UiFormatters.historyDateLabel(it.scheduledAtMillis) }
            .map { (label, entries) -> HistoryGroupUi(label = label, entries = entries) }
    }

    // Maps one database history row into localized text used by the history screen.
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

    // Summarizes the weekly bars into a short sentence above the chart.
    private fun weeklySummary(bars: List<WeeklyOverviewBarUi>): String {
        val totalDue = bars.sumOf { it.dueCount }
        val totalTaken = bars.sumOf { it.takenCount }
        return if (totalDue == 0) {
            "Diese Woche sind keine Erinnerungen geplant."
        } else {
            "Diese Woche: $totalTaken von $totalDue geplanten Einnahmen bestätigt."
        }
    }

    // Calculates planned and confirmed intakes for each day of the current Monday-to-Sunday week.
    private fun weeklyBars(
        medications: List<MedicationEntity>,
        history: List<IntakeHistoryEntity>
    ): List<WeeklyOverviewBarUi> {
        val today = Calendar.getInstance()
        val weekStart = startOfWeek(today)
        return (0 until 7).map { dayOffset ->
            val day = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            val dayStartMillis = startOfDayMillis(day)
            val dayEndMillis = endOfDayMillis(day)

            val dueCount = medications.count { medication ->
                medication.isScheduledFor(day, dayStartMillis)
            }
            val takenCount = history.asSequence()
                .filter {
                    it.status == IntakeStatus.TAKEN &&
                        it.scheduledAtMillis in dayStartMillis..dayEndMillis
                }
                .map { "${it.medicationId}_${it.scheduledAtMillis}" }
                .distinct()
                .count()
                .coerceAtMost(dueCount)

            WeeklyOverviewBarUi(
                label = weekdayLabel(day),
                dueCount = dueCount,
                takenCount = takenCount,
                isToday = UiFormatters.sameDay(today, day)
            )
        }
    }

    // Checks whether a medication should count toward a given day in the weekly overview.
    private fun MedicationEntity.isScheduledFor(
        day: Calendar,
        dayStartMillis: Long
    ): Boolean {
        val createdDay = Calendar.getInstance().apply {
            timeInMillis = createdAtMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (createdDay.timeInMillis > dayStartMillis) return false

        return when (repeatOption) {
            RepeatOption.DAILY -> true
            RepeatOption.WEEKLY -> reminderDayOfWeek == day.get(Calendar.DAY_OF_WEEK)
            RepeatOption.ONCE -> UiFormatters.sameDay(createdDay, day)
        }
    }

    // Normalizes any date to the Monday that starts the currently displayed week.
    private fun startOfWeek(reference: Calendar): Calendar {
        return (reference.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun startOfDayMillis(calendar: Calendar): Long {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDayMillis(calendar: Calendar): Long {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun weekdayLabel(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mo"
            Calendar.TUESDAY -> "Di"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Do"
            Calendar.FRIDAY -> "Fr"
            Calendar.SATURDAY -> "Sa"
            else -> "So"
        }
    }
}
