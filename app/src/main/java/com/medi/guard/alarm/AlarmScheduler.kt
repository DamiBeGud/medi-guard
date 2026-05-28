package com.medi.guard.alarm

import com.medi.guard.data.room.RepeatOption

interface AlarmScheduler {
    fun scheduleMedicationAlarm(
        medicationId: Long,
        hour: Int,
        minute: Int,
        repeatOption: RepeatOption,
        reminderDayOfWeek: Int? = null
    )

    fun scheduleSnoozeAlarm(
        medicationId: Long,
        triggerAtMillis: Long,
        scheduledAtMillis: Long
    )

    fun cancelMedicationAlarm(medicationId: Long)

    fun rescheduleAllDirectBootAlarms()
}
