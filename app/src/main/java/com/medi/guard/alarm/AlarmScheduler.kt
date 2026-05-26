package com.medi.guard.alarm

interface AlarmScheduler {
    fun scheduleMedicationAlarm(
        medicationId: Long,
        hour: Int,
        minute: Int
    )

    fun scheduleSnoozeAlarm(
        medicationId: Long,
        triggerAtMillis: Long,
        scheduledAtMillis: Long
    )

    fun cancelMedicationAlarm(medicationId: Long)

    fun rescheduleAllDirectBootAlarms()
}
