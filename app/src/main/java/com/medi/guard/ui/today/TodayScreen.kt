package com.medi.guard.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medi.guard.ui.components.ClinicalCard
import com.medi.guard.ui.components.LargeNeutralButton
import com.medi.guard.ui.components.LargePrimaryButton
import com.medi.guard.ui.components.StatusChip
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSurfaceContainer
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant

@Composable
fun TodayScreen(
    state: TodayUiState,
    onMedicationTaken: (Long, Long) -> Unit,
    onSnoozeMedication: (Long, Long) -> Unit,
    onAddMedicationClick: () -> Unit,
    onMedicationClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 28.dp,
            end = 20.dp,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Guten Morgen",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ClinicalText
                )
                Text(
                    text = "Ihre Medikamente für heute",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClinicalTextVariant
                )
            }
        }

        item { SafetyStatusCard() }

        if (state.reminders.isEmpty()) {
            item {
                EmptyMedicationState(onAddMedicationClick = onAddMedicationClick)
            }
        } else {
            items(state.reminders, key = { it.id }) { reminder ->
                MedicationReminderCard(
                    reminder = reminder,
                    onTakenClick = {
                        onMedicationTaken(reminder.id, reminder.scheduledAtMillis)
                    },
                    onSnoozeClick = {
                        onSnoozeMedication(reminder.id, reminder.scheduledAtMillis)
                    },
                    onMedicationClick = {
                        onMedicationClick(reminder.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun SafetyStatusCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicalSuccessContainer, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = ClinicalSuccess,
                modifier = Modifier.size(32.dp)
            )
        }
        Column {
            Text(
                text = "Sichere Erinnerungen aktiv",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = ClinicalSuccess
            )
            Text(
                text = "• Aktiv",
                style = MaterialTheme.typography.headlineMedium,
                color = ClinicalSuccess
            )
        }
    }
}

@Composable
private fun EmptyMedicationState(onAddMedicationClick: () -> Unit) {
    ClinicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.HealthAndSafety,
                contentDescription = null,
                tint = ClinicalPrimary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Noch keine Medikamente",
                style = MaterialTheme.typography.headlineMedium,
                color = ClinicalText
            )
            Text(
                text = "Fügen Sie Ihr erstes Medikament hinzu, damit MediGuard Sie erinnern kann.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalTextVariant
            )
            LargePrimaryButton(
                text = "Medikament hinzufügen",
                icon = Icons.Filled.Medication,
                onClick = onAddMedicationClick
            )
        }
    }
}

@Composable
private fun MedicationReminderCard(
    reminder: TodayMedicationUi,
    onTakenClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onMedicationClick: () -> Unit
) {
    ClinicalCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMedicationClick)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = reminder.time,
                        style = MaterialTheme.typography.displayLarge,
                        color = ClinicalPrimary
                    )
                    StatusChip(status = reminder.status)
                }

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(ClinicalSurfaceContainer, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Medication,
                            contentDescription = null,
                            tint = ClinicalPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reminder.medicationType,
                            style = MaterialTheme.typography.labelLarge,
                            color = ClinicalTextVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ClinicalText
                )
                Text(
                    text = "Dosierung: ${reminder.dosage}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClinicalTextVariant
                )
            }

            LargePrimaryButton(
                text = "Eingenommen",
                icon = Icons.Filled.CheckCircle,
                onClick = onTakenClick,
                enabled = reminder.status != ReminderStatus.TAKEN
            )
            LargeNeutralButton(
                text = "Später erinnern",
                icon = Icons.Filled.Alarm,
                onClick = onSnoozeClick,
                enabled = reminder.status != ReminderStatus.TAKEN
            )
        }
    }
}
