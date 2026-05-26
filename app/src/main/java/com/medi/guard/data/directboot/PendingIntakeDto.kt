package com.medi.guard.data.directboot

data class PendingIntakeDto(
    val key: String,
    val medicationId: Long,
    val scheduledAtMillis: Long,
    val takenAtMillis: Long
)
