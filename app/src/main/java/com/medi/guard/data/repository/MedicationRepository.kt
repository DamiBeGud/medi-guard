package com.medi.guard.data.repository

import com.medi.guard.alarm.AlarmScheduler
import com.medi.guard.data.directboot.DirectBootAlarmDto
import com.medi.guard.data.directboot.DirectBootAlarmStore
import com.medi.guard.data.directboot.PendingIntakeStore
import com.medi.guard.data.room.IntakeHistoryDao
import com.medi.guard.data.room.IntakeHistoryEntity
import com.medi.guard.data.room.IntakeStatus
import com.medi.guard.data.room.MedicationDao
import com.medi.guard.data.room.MedicationEntity
import com.medi.guard.data.room.RepeatOption
import com.medi.guard.ui.UiFormatters
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val historyDao: IntakeHistoryDao,
    private val directBootAlarmStore: DirectBootAlarmStore,
    private val pendingIntakeStore: PendingIntakeStore,
    private val alarmScheduler: AlarmScheduler
) {
    val medications: Flow<List<MedicationEntity>> = medicationDao.observeMedications()
    val activeMedications: Flow<List<MedicationEntity>> = medicationDao.observeActiveMedications()
    val history: Flow<List<IntakeHistoryEntity>> = historyDao.observeHistory()

    // Streams a single medication so detail and edit screens react to database updates.
    fun observeMedication(medicationId: Long): Flow<MedicationEntity?> {
        return medicationDao.observeMedication(medicationId)
    }

    // Streams the history timeline for one medication detail screen.
    fun observeHistoryForMedication(medicationId: Long): Flow<List<IntakeHistoryEntity>> {
        return historyDao.observeHistoryForMedication(medicationId)
    }

    // Loads one medication for one-off actions such as receivers and detail commands.
    suspend fun getMedication(medicationId: Long): MedicationEntity? = withContext(Dispatchers.IO) {
        medicationDao.getMedication(medicationId)
    }

    // Creates the medication, mirrors minimal alarm data to Device Protected Storage, and schedules its alarm.
    suspend fun addMedication(
        name: String,
        medicationType: String,
        dosageAmount: String,
        dosageUnit: String,
        hour: Int,
        minute: Int,
        repeatOption: RepeatOption,
        reminderDayOfWeek: Int?
    ): Long = withContext(Dispatchers.IO) {
        val medicationId = medicationDao.insertMedication(
            MedicationEntity(
                name = name.trim(),
                medicationType = medicationType.trim(),
                dosageAmount = dosageAmount.trim(),
                dosageUnit = dosageUnit.trim(),
                reminderHour = hour,
                reminderMinute = minute,
                repeatOption = repeatOption,
                reminderDayOfWeek = reminderDayOfWeek,
                isActive = true
            )
        )
        saveDirectBootMetadata(medicationId, hour, minute, repeatOption, reminderDayOfWeek)
        alarmScheduler.scheduleMedicationAlarm(
            medicationId,
            hour,
            minute,
            repeatOption,
            reminderDayOfWeek
        )
        medicationId
    }

    // Updates the medication definition and refreshes the alarm if the reminder is active.
    suspend fun updateMedication(
        medicationId: Long,
        name: String,
        medicationType: String,
        dosageAmount: String,
        dosageUnit: String,
        hour: Int,
        minute: Int,
        repeatOption: RepeatOption,
        reminderDayOfWeek: Int?
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = medicationDao.getMedication(medicationId) ?: return@withContext false
        val updated = existing.copy(
            name = name.trim(),
            medicationType = medicationType.trim(),
            dosageAmount = dosageAmount.trim(),
            dosageUnit = dosageUnit.trim(),
            reminderHour = hour,
            reminderMinute = minute,
            repeatOption = repeatOption,
            reminderDayOfWeek = reminderDayOfWeek
        )
        medicationDao.updateMedication(updated)

        if (updated.isActive) {
            saveDirectBootMetadata(medicationId, hour, minute, repeatOption, reminderDayOfWeek)
            alarmScheduler.scheduleMedicationAlarm(
                medicationId,
                hour,
                minute,
                repeatOption,
                reminderDayOfWeek
            )
        }
        true
    }

    // Pauses or resumes scheduling without deleting the medication entry itself.
    suspend fun setMedicationPaused(medicationId: Long, paused: Boolean): Boolean = withContext(Dispatchers.IO) {
        val existing = medicationDao.getMedication(medicationId) ?: return@withContext false
        medicationDao.setMedicationActive(medicationId, !paused)
        if (paused) {
            directBootAlarmStore.remove(medicationId)
            alarmScheduler.cancelMedicationAlarm(medicationId)
        } else {
            saveDirectBootMetadata(
                medicationId = medicationId,
                hour = existing.reminderHour,
                minute = existing.reminderMinute,
                repeatOption = existing.repeatOption,
                reminderDayOfWeek = existing.reminderDayOfWeek
            )
            alarmScheduler.scheduleMedicationAlarm(
                medicationId = medicationId,
                hour = existing.reminderHour,
                minute = existing.reminderMinute,
                repeatOption = existing.repeatOption,
                reminderDayOfWeek = existing.reminderDayOfWeek
            )
        }
        true
    }

    // Deletes the medication and removes every alarm artifact tied to it.
    suspend fun deleteMedication(medicationId: Long): Boolean = withContext(Dispatchers.IO) {
        medicationDao.getMedication(medicationId) ?: return@withContext false
        medicationDao.deleteMedication(medicationId)
        directBootAlarmStore.remove(medicationId)
        alarmScheduler.cancelMedicationAlarm(medicationId)
        true
    }

    // Inserts a confirmed intake row into history using the medication's current display dosage.
    suspend fun markMedicationTaken(
        medicationId: Long,
        scheduledAtMillis: Long? = null,
        takenAtMillis: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        val medication = medicationDao.getMedication(medicationId) ?: return@withContext false
        historyDao.insertHistory(
            IntakeHistoryEntity(
                medicationId = medicationId,
                medicationName = medication.name,
                dosage = UiFormatters.dosage(medication.dosageAmount, medication.dosageUnit),
                scheduledAtMillis = scheduledAtMillis
                    ?: scheduledTimeForToday(medication.reminderHour, medication.reminderMinute),
                takenAtMillis = takenAtMillis,
                status = IntakeStatus.TAKEN
            )
        )
        true
    }

    // Delegates the "remind me later" action to the alarm scheduler with a relative delay.
    fun snoozeMedication(medicationId: Long, scheduledAtMillis: Long, minutes: Int = 15) {
        alarmScheduler.scheduleSnoozeAlarm(
            medicationId = medicationId,
            triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L,
            scheduledAtMillis = scheduledAtMillis
        )
    }

    // Flushes lock-screen confirmations into Room once the user has unlocked the device.
    suspend fun reconcilePendingIntakes() = withContext(Dispatchers.IO) {
        val pending = pendingIntakeStore.getAll()
        val clearedKeys = mutableListOf<String>()
        pending.forEach { item ->
            val saved = markMedicationTaken(
                medicationId = item.medicationId,
                scheduledAtMillis = item.scheduledAtMillis,
                takenAtMillis = item.takenAtMillis
            )
            if (saved) {
                clearedKeys += item.key
            }
        }
        pendingIntakeStore.clear(clearedKeys)
    }

    // Stores only alarm metadata that is safe to read before the first user unlock after reboot.
    private fun saveDirectBootMetadata(
        medicationId: Long,
        hour: Int,
        minute: Int,
        repeatOption: RepeatOption,
        reminderDayOfWeek: Int?
    ) {
        directBootAlarmStore.save(
            DirectBootAlarmDto(
                medicationId = medicationId,
                hour = hour,
                minute = minute,
                requestCode = DirectBootAlarmStore.requestCodeForMedication(medicationId),
                repeatOption = repeatOption,
                reminderDayOfWeek = reminderDayOfWeek
            )
        )
    }

    companion object {
        // Rebuilds today's planned timestamp for status and history calculations.
        fun scheduledTimeForToday(hour: Int, minute: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Lower time boundary used for "today" history and reminder lookups.
        fun startOfTodayMillis(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Upper time boundary used for "today" history and reminder lookups.
        fun endOfTodayMillis(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }
    }
}
