package com.medi.guard.data.directboot

data class DirectBootAlarmDto(
    val medicationId: Long,
    val hour: Int,
    val minute: Int,
    val requestCode: Int,
    val repeatsDaily: Boolean
)
