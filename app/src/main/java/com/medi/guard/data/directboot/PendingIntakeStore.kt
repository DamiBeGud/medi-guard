package com.medi.guard.data.directboot

import android.content.Context

class PendingIntakeStore(context: Context) {
    /*
     * IMPORTANT: Pending confirmations may be written while the user is still
     * locked after reboot. Store only IDs and times here. Medication names and
     * dosages remain in Credential Encrypted Room storage.
     */
    private val protectedContext = context.createDeviceProtectedStorageContext()
    private val prefs = protectedContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePendingTaken(medicationId: Long, scheduledAtMillis: Long, takenAtMillis: Long) {
        val entryKey = "${medicationId}_${scheduledAtMillis}_${takenAtMillis}"
        val keys = pendingKeys().toMutableSet()
        keys += entryKey

        prefs.edit()
            .putStringSet(KEY_PENDING_KEYS, keys)
            .putLong(key(entryKey, "medicationId"), medicationId)
            .putLong(key(entryKey, "scheduledAtMillis"), scheduledAtMillis)
            .putLong(key(entryKey, "takenAtMillis"), takenAtMillis)
            .apply()
    }

    fun getAll(): List<PendingIntakeDto> {
        return pendingKeys().mapNotNull { entryKey ->
            val medicationId = prefs.getLong(key(entryKey, "medicationId"), -1L)
            val scheduledAtMillis = prefs.getLong(key(entryKey, "scheduledAtMillis"), -1L)
            val takenAtMillis = prefs.getLong(key(entryKey, "takenAtMillis"), -1L)

            if (medicationId > 0 && scheduledAtMillis > 0 && takenAtMillis > 0) {
                PendingIntakeDto(
                    key = entryKey,
                    medicationId = medicationId,
                    scheduledAtMillis = scheduledAtMillis,
                    takenAtMillis = takenAtMillis
                )
            } else {
                null
            }
        }
    }

    fun clear(keysToClear: Collection<String>) {
        if (keysToClear.isEmpty()) return

        val remainingKeys = pendingKeys().toMutableSet()
        val editor = prefs.edit()
        keysToClear.forEach { entryKey ->
            remainingKeys -= entryKey
            editor
                .remove(key(entryKey, "medicationId"))
                .remove(key(entryKey, "scheduledAtMillis"))
                .remove(key(entryKey, "takenAtMillis"))
        }
        editor.putStringSet(KEY_PENDING_KEYS, remainingKeys).apply()
    }

    private fun pendingKeys(): Set<String> {
        return prefs.getStringSet(KEY_PENDING_KEYS, emptySet()).orEmpty()
    }

    private fun key(entryKey: String, suffix: String): String = "pending_${entryKey}_$suffix"

    companion object {
        private const val PREFS_NAME = "direct_boot_pending_intake"
        private const val KEY_PENDING_KEYS = "pending_keys"
    }
}
