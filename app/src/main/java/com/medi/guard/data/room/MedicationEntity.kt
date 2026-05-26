package com.medi.guard.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val reminderHour: Int,
    val reminderMinute: Int,
    val repeatOption: RepeatOption = RepeatOption.DAILY,
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
