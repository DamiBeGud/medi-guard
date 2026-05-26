package com.medi.guard.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intake_history",
    indices = [
        Index(value = ["medicationId"]),
        Index(value = ["scheduledAtMillis"])
    ]
)
data class IntakeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledAtMillis: Long,
    val takenAtMillis: Long?,
    val status: IntakeStatus
)
