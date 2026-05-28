package com.medi.guard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import com.medi.guard.MediGuardApplication
import com.medi.guard.data.room.RepeatOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as MediGuardApplication
        when (intent.action) {
            ACTION_MEDICATION_ALARM -> handleMedicationAlarm(context, app, intent)
            ACTION_MARK_TAKEN -> handleMarkTaken(context, app, intent)
            ACTION_SNOOZE -> handleSnooze(app, intent)
        }
    }

    private fun handleMedicationAlarm(
        context: Context,
        app: MediGuardApplication,
        intent: Intent
    ) {
        /*
         * IMPORTANT: This receiver is Direct Boot aware and may run before the
         * first user unlock after reboot. While locked, do not access Room or
         * any Credential Encrypted storage. The notification must remain generic.
         */
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (medicationId <= 0L) return

        val hour = intent.getIntExtra(EXTRA_REMINDER_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_REMINDER_MINUTE, -1)
        val scheduledAtMillis = intent.getLongExtra(
            EXTRA_SCHEDULED_AT_MILLIS,
            System.currentTimeMillis()
        )
        val isSnooze = intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)

        if (!isSnooze) {
            val metadata = app.directBootAlarmStore.getAll().firstOrNull {
                it.medicationId == medicationId
            }
            if (metadata != null && metadata.repeatOption != RepeatOption.ONCE) {
                app.alarmScheduler.scheduleMedicationAlarm(
                    medicationId = medicationId,
                    hour = metadata.hour,
                    minute = metadata.minute,
                    repeatOption = metadata.repeatOption,
                    reminderDayOfWeek = metadata.reminderDayOfWeek
                )
            } else {
                app.directBootAlarmStore.remove(medicationId)
            }
        }

        if (!isUserUnlocked(context)) {
            app.medicationNotificationManager.showGenericReminder(
                medicationId = medicationId,
                scheduledAtMillis = scheduledAtMillis,
                hour = hour,
                minute = minute
            )
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val medication = app.medicationRepository.getMedication(medicationId)
                if (medication != null && medication.isActive) {
                    app.medicationNotificationManager.showDetailedReminder(
                        medication = medication,
                        scheduledAtMillis = scheduledAtMillis
                    )
                } else {
                    app.medicationNotificationManager.showGenericReminder(
                        medicationId = medicationId,
                        scheduledAtMillis = scheduledAtMillis,
                        hour = hour,
                        minute = minute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMarkTaken(
        context: Context,
        app: MediGuardApplication,
        intent: Intent
    ) {
        /*
         * If the user taps a notification action before first unlock, CE Room is
         * still unavailable. Store only a pending ID/time in Device Protected
         * Storage and reconcile it after unlock.
         */
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (medicationId <= 0L) return

        val scheduledAtMillis = intent.getLongExtra(
            EXTRA_SCHEDULED_AT_MILLIS,
            System.currentTimeMillis()
        )
        val takenAtMillis = System.currentTimeMillis()
        app.medicationNotificationManager.cancelReminder(medicationId)

        if (!isUserUnlocked(context)) {
            app.pendingIntakeStore.savePendingTaken(
                medicationId = medicationId,
                scheduledAtMillis = scheduledAtMillis,
                takenAtMillis = takenAtMillis
            )
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.medicationRepository.markMedicationTaken(
                    medicationId = medicationId,
                    scheduledAtMillis = scheduledAtMillis,
                    takenAtMillis = takenAtMillis
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleSnooze(app: MediGuardApplication, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        if (medicationId <= 0L) return

        val scheduledAtMillis = intent.getLongExtra(
            EXTRA_SCHEDULED_AT_MILLIS,
            System.currentTimeMillis()
        )
        app.medicationNotificationManager.cancelReminder(medicationId)
        app.alarmScheduler.scheduleSnoozeAlarm(
            medicationId = medicationId,
            triggerAtMillis = System.currentTimeMillis() + 15 * 60_000L,
            scheduledAtMillis = scheduledAtMillis
        )
    }

    private fun isUserUnlocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
        return context.getSystemService(UserManager::class.java).isUserUnlocked
    }

    companion object {
        const val ACTION_MEDICATION_ALARM = "com.medi.guard.action.MEDICATION_ALARM"
        const val ACTION_MARK_TAKEN = "com.medi.guard.action.MARK_TAKEN"
        const val ACTION_SNOOZE = "com.medi.guard.action.SNOOZE"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
        const val EXTRA_SCHEDULED_AT_MILLIS = "extra_scheduled_at_millis"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}
