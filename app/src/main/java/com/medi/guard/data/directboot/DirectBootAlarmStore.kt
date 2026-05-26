package com.medi.guard.data.directboot

import android.content.Context
import kotlin.math.absoluteValue

class DirectBootAlarmStore(context: Context) {
    /*
     * IMPORTANT: This store lives in Device Protected Storage so it can be read
     * before the first user unlock after a reboot. It must never contain
     * medication names, dosages, notes, or other CE-only health details.
     */
    private val protectedContext = context.createDeviceProtectedStorageContext()
    private val prefs = protectedContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(dto: DirectBootAlarmDto) {
        val ids = medicationIds().toMutableSet()
        ids += dto.medicationId.toString()

        prefs.edit()
            .putStringSet(KEY_IDS, ids)
            .putInt(key(dto.medicationId, "hour"), dto.hour)
            .putInt(key(dto.medicationId, "minute"), dto.minute)
            .putInt(key(dto.medicationId, "requestCode"), dto.requestCode)
            .putBoolean(key(dto.medicationId, "repeatsDaily"), dto.repeatsDaily)
            .apply()
    }

    fun remove(medicationId: Long) {
        val ids = medicationIds().toMutableSet()
        ids -= medicationId.toString()

        prefs.edit()
            .putStringSet(KEY_IDS, ids)
            .remove(key(medicationId, "hour"))
            .remove(key(medicationId, "minute"))
            .remove(key(medicationId, "requestCode"))
            .remove(key(medicationId, "repeatsDaily"))
            .apply()
    }

    fun getAll(): List<DirectBootAlarmDto> {
        return medicationIds().mapNotNull { rawId ->
            val medicationId = rawId.toLongOrNull() ?: return@mapNotNull null
            val hour = prefs.getInt(key(medicationId, "hour"), -1)
            val minute = prefs.getInt(key(medicationId, "minute"), -1)
            val requestCode = prefs.getInt(
                key(medicationId, "requestCode"),
                requestCodeForMedication(medicationId)
            )
            val repeatsDaily = prefs.getBoolean(key(medicationId, "repeatsDaily"), true)

            if (hour in 0..23 && minute in 0..59) {
                DirectBootAlarmDto(
                    medicationId = medicationId,
                    hour = hour,
                    minute = minute,
                    requestCode = requestCode,
                    repeatsDaily = repeatsDaily
                )
            } else {
                null
            }
        }
    }

    private fun medicationIds(): Set<String> {
        return prefs.getStringSet(KEY_IDS, emptySet()).orEmpty()
    }

    private fun key(medicationId: Long, suffix: String): String = "alarm_${medicationId}_$suffix"

    companion object {
        private const val PREFS_NAME = "direct_boot_alarm_metadata"
        private const val KEY_IDS = "medication_ids"

        fun requestCodeForMedication(medicationId: Long): Int {
            return (medicationId.hashCode().absoluteValue % 800_000) + 10_000
        }
    }
}
