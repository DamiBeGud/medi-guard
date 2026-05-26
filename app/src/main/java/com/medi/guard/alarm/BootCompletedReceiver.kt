package com.medi.guard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import com.medi.guard.MediGuardApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as MediGuardApplication
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                /*
                 * IMPORTANT: Locked boot happens before first unlock. Do not read
                 * Room or any Credential Encrypted storage here. Alarms are restored
                 * only from minimal Device Protected Storage metadata.
                 */
                app.alarmScheduler.rescheduleAllDirectBootAlarms()
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_UNLOCKED -> {
                app.alarmScheduler.rescheduleAllDirectBootAlarms()
                reconcilePendingIfUnlocked(context, app)
            }
        }
    }

    private fun reconcilePendingIfUnlocked(context: Context, app: MediGuardApplication) {
        if (!isUserUnlocked(context)) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.medicationRepository.reconcilePendingIntakes()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isUserUnlocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
        return context.getSystemService(UserManager::class.java).isUserUnlocked
    }
}
