package com.medi.guard.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UiFormatters {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
    private val dateFormat = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY)

    fun time(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

    fun timeWithUhr(hour: Int, minute: Int): String = "${time(hour, minute)} Uhr"

    fun timeWithUhr(millis: Long): String = "${timeFormat.format(millis)} Uhr"

    fun historyDateLabel(millis: Long): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return when {
            sameDay(today, target) -> "Heute"
            sameDay(yesterday, target) -> "Gestern"
            else -> dateFormat.format(millis)
        }
    }

    fun sameDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }
}
