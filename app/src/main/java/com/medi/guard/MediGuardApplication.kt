package com.medi.guard

import android.app.Application
import com.medi.guard.alarm.AndroidAlarmScheduler
import com.medi.guard.data.directboot.DirectBootAlarmStore
import com.medi.guard.data.directboot.PendingIntakeStore
import com.medi.guard.data.preferences.UserPreferencesRepository
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.MediGuardDatabase
import com.medi.guard.notification.MedicationNotificationManager

class MediGuardApplication : Application() {
    val directBootAlarmStore by lazy { DirectBootAlarmStore(this) }
    val pendingIntakeStore by lazy { PendingIntakeStore(this) }
    val alarmScheduler by lazy { AndroidAlarmScheduler(this, directBootAlarmStore) }
    val medicationNotificationManager by lazy { MedicationNotificationManager(this) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    /*
     * Room uses normal Credential Encrypted app storage. Do not touch this lazy
     * database from Direct Boot paths before the user has unlocked the device.
     */
    private val database by lazy { MediGuardDatabase.getInstance(this) }

    val medicationRepository by lazy {
        MedicationRepository(
            medicationDao = database.medicationDao(),
            historyDao = database.intakeHistoryDao(),
            directBootAlarmStore = directBootAlarmStore,
            pendingIntakeStore = pendingIntakeStore,
            alarmScheduler = alarmScheduler
        )
    }

    override fun onCreate() {
        super.onCreate()
        medicationNotificationManager.createNotificationChannel()
    }
}
