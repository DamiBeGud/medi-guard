package com.medi.guard.data.room

import androidx.room.TypeConverter

class MediGuardConverters {
    @TypeConverter
    fun intakeStatusFromString(value: String): IntakeStatus = IntakeStatus.valueOf(value)

    @TypeConverter
    fun intakeStatusToString(value: IntakeStatus): String = value.name

    @TypeConverter
    fun repeatOptionFromString(value: String): RepeatOption = RepeatOption.valueOf(value)

    @TypeConverter
    fun repeatOptionToString(value: RepeatOption): String = value.name
}
