package com.medi.guard.data.directboot

import com.medi.guard.data.room.RepeatOption

data class DirectBootAlarmDto(
    val medicationId: Long,
    val hour: Int,
    val minute: Int,
    val requestCode: Int,
    val repeatOption: RepeatOption,
    val reminderDayOfWeek: Int?
)
