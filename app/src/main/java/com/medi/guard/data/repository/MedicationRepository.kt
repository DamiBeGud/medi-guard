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

    fun observeMedication(medicationId: Long): Flow<MedicationEntity?> {
        return medicationDao.observeMedication(medicationId)
    }

    fun observeHistoryForMedication(medicationId: Long): Flow<List<IntakeHistoryEntity>> {
        return historyDao.observeHistoryForMedication(medicationId)
    }

    suspend fun getMedication(medicationId: Long): MedicationEntity? = withContext(Dispatchers.IO) {
        medicationDao.getMedication(medicationId)
    }

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

    suspend fun deleteMedication(medicationId: Long): Boolean = withContext(Dispatchers.IO) {
        medicationDao.getMedication(medicationId) ?: return@withContext false
        medicationDao.deleteMedication(medicationId)
        directBootAlarmStore.remove(medicationId)
        alarmScheduler.cancelMedicationAlarm(medicationId)
        true
    }

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

    fun snoozeMedication(medicationId: Long, scheduledAtMillis: Long, minutes: Int = 15) {
        alarmScheduler.scheduleSnoozeAlarm(
            medicationId = medicationId,
            triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L,
            scheduledAtMillis = scheduledAtMillis
        )
    }

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
        fun scheduledTimeForToday(hour: Int, minute: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        fun startOfTodayMillis(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

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
