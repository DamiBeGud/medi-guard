package com.medi.guard.notification

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.medi.guard.MainActivity
import com.medi.guard.R
import com.medi.guard.alarm.MedicationAlarmReceiver
import com.medi.guard.data.room.MedicationEntity
import com.medi.guard.ui.UiFormatters

class MedicationNotificationManager(private val context: Context) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Medikamenten-Erinnerungen",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pünktliche Erinnerungen für geplante Einnahmen"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showGenericReminder(
        medicationId: Long,
        scheduledAtMillis: Long,
        hour: Int,
        minute: Int
    ) {
        showReminder(
            medicationId = medicationId,
            scheduledAtMillis = scheduledAtMillis,
            hour = hour,
            minute = minute,
            title = "MediGuard Erinnerung",
            body = "Zeit für Ihre Medikamente!"
        )
    }

    fun showDetailedReminder(medication: MedicationEntity, scheduledAtMillis: Long) {
        val dosageText = UiFormatters.dosage(medication.dosageAmount, medication.dosageUnit)
        showReminder(
            medicationId = medication.id,
            scheduledAtMillis = scheduledAtMillis,
            hour = medication.reminderHour,
            minute = medication.reminderMinute,
            title = "MediGuard Erinnerung",
            body = "Bitte nehmen Sie jetzt $dosageText ${medication.name} ein."
        )
    }

    fun cancelReminder(medicationId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(medicationId))
    }

    @SuppressLint("MissingPermission")
    private fun showReminder(
        medicationId: Long,
        scheduledAtMillis: Long,
        hour: Int,
        minute: Int,
        title: String,
        body: String
    ) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOngoing(false)
            .setContentIntent(contentIntent())
            .addAction(
                R.drawable.ic_notification,
                "Später erinnern",
                actionIntent(
                    medicationId = medicationId,
                    scheduledAtMillis = scheduledAtMillis,
                    hour = hour,
                    minute = minute,
                    action = MedicationAlarmReceiver.ACTION_SNOOZE
                )
            )
            .addAction(
                R.drawable.ic_notification,
                "Eingenommen",
                actionIntent(
                    medicationId = medicationId,
                    scheduledAtMillis = scheduledAtMillis,
                    hour = hour,
                    minute = minute,
                    action = MedicationAlarmReceiver.ACTION_MARK_TAKEN
                )
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId(medicationId), notification)
        } catch (_: SecurityException) {
            // Notification permission can be revoked after our check; skip showing the reminder.
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun contentIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionIntent(
        medicationId: Long,
        scheduledAtMillis: Long,
        hour: Int,
        minute: Int,
        action: String
    ): PendingIntent {
        val requestCode = medicationId.hashCode() + action.hashCode()
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
            putExtra(MedicationAlarmReceiver.EXTRA_REMINDER_HOUR, hour)
            putExtra(MedicationAlarmReceiver.EXTRA_REMINDER_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationId(medicationId: Long): Int {
        return (medicationId.hashCode() and 0x00FFFFFF) + 100
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
    }
}
