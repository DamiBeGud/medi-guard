package com.medi.guard.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medi.guard.MainActivity
import com.medi.guard.data.directboot.DirectBootAlarmStore
import java.util.Calendar

class AndroidAlarmScheduler(
    private val context: Context,
    private val directBootAlarmStore: DirectBootAlarmStore
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleMedicationAlarm(medicationId: Long, hour: Int, minute: Int) {
        val requestCode = DirectBootAlarmStore.requestCodeForMedication(medicationId)
        val triggerAtMillis = nextTriggerMillis(hour, minute)
        val operation = alarmPendingIntent(
            medicationId = medicationId,
            requestCode = requestCode,
            hour = hour,
            minute = minute,
            scheduledAtMillis = triggerAtMillis,
            isSnooze = false,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(triggerAtMillis, requestCode, operation)
    }

    override fun scheduleSnoozeAlarm(
        medicationId: Long,
        triggerAtMillis: Long,
        scheduledAtMillis: Long
    ) {
        val requestCode = DirectBootAlarmStore.requestCodeForMedication(medicationId) + SNOOZE_OFFSET
        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        val operation = alarmPendingIntent(
            medicationId = medicationId,
            requestCode = requestCode,
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            scheduledAtMillis = scheduledAtMillis,
            isSnooze = true,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(triggerAtMillis, requestCode, operation)
    }

    override fun cancelMedicationAlarm(medicationId: Long) {
        val requestCode = DirectBootAlarmStore.requestCodeForMedication(medicationId)
        alarmPendingIntent(
            medicationId = medicationId,
            requestCode = requestCode,
            hour = 0,
            minute = 0,
            scheduledAtMillis = 0L,
            isSnooze = false,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it) }
        alarmPendingIntent(
                medicationId = medicationId,
                requestCode = requestCode + SNOOZE_OFFSET,
                hour = 0,
                minute = 0,
                scheduledAtMillis = 0L,
                isSnooze = true,
                flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it) }
    }

    override fun rescheduleAllDirectBootAlarms() {
        /*
         * IMPORTANT: This method is used from LOCKED_BOOT_COMPLETED. It reads only
         * Device Protected Storage alarm metadata and must not touch Room or CE data.
         */
        directBootAlarmStore.getAll().forEach { dto ->
            if (dto.repeatsDaily) {
                scheduleMedicationAlarm(dto.medicationId, dto.hour, dto.minute)
            }
        }
    }

    private fun alarmPendingIntent(
        medicationId: Long,
        requestCode: Int,
        hour: Int,
        minute: Int,
        scheduledAtMillis: Long,
        isSnooze: Boolean,
        flags: Int
    ): PendingIntent? {
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = MedicationAlarmReceiver.ACTION_MEDICATION_ALARM
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_REMINDER_HOUR, hour)
            putExtra(MedicationAlarmReceiver.EXTRA_REMINDER_MINUTE, minute)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
            putExtra(MedicationAlarmReceiver.EXTRA_IS_SNOOZE, isSnooze)
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun setExact(triggerAtMillis: Long, requestCode: Int, operation: PendingIntent?) {
        operation ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val showIntent = PendingIntent.getActivity(
                context,
                requestCode,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                operation
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    companion object {
        private const val SNOOZE_OFFSET = 900_000
    }
}
