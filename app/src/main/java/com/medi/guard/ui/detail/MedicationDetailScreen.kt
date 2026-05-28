package com.medi.guard.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medi.guard.ui.components.ClinicalCard
import com.medi.guard.ui.components.IconLabelChip
import com.medi.guard.ui.components.LargeDangerButton
import com.medi.guard.ui.components.LargeNeutralButton
import com.medi.guard.ui.components.LargePrimaryButton
import com.medi.guard.ui.components.LargeWarningButton
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalSurfaceContainer
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant

@Composable
fun MedicationDetailScreen(
    state: MedicationDetailUiState,
    onTakenClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onEditClick: () -> Unit,
    onPauseClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val medication = state.medication

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (medication == null) {
            ClinicalCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Medikament nicht gefunden",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClinicalText
                    )
                    Text(
                        text = "Dieser Eintrag ist nicht mehr verfügbar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClinicalTextVariant
                    )
                }
            }
            return@Column
        }

        ClinicalCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = state.reminderTime,
                            style = MaterialTheme.typography.displayLarge,
                            color = ClinicalPrimary
                        )
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = ClinicalText
                        )
                        Text(
                            text = "${state.dosageText} ${medication.medicationType}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClinicalTextVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(ClinicalPrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Medication,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                IconLabelChip(
                    icon = if (medication.isActive) Icons.Filled.CheckCircle else Icons.Filled.PauseCircle,
                    text = if (medication.isActive) "Täglich aktiv" else "Pausiert",
                    containerColor = ClinicalSuccessContainer.copy(alpha = 0.38f),
                    contentColor = ClinicalSuccess
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClinicalSurfaceContainer, RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HINWEISE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClinicalTextVariant
                    )
                    Text(
                        text = state.lastTakenText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClinicalText
                    )
                }
            }
        }

        LargePrimaryButton(
            text = "Als eingenommen markieren",
            icon = Icons.Filled.CheckCircle,
            onClick = onTakenClick
        )
        LargeNeutralButton(
            text = "Später erinnern",
            icon = Icons.Filled.Alarm,
            onClick = onSnoozeClick
        )
        LargeNeutralButton(
            text = "Bearbeiten",
            icon = Icons.Filled.Edit,
            onClick = onEditClick
        )
        LargeWarningButton(
            text = if (medication.isActive) "Pausieren" else "Aktivieren",
            icon = if (medication.isActive) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
            onClick = onPauseClick
        )
        LargeDangerButton(
            text = "Löschen",
            icon = Icons.Filled.Delete,
            onClick = { showDeleteDialog = true }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = "Medikament löschen?")
            },
            text = {
                Text(text = "Diese Erinnerung wird entfernt. Der Verlauf bleibt erhalten.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    }
                ) {
                    Text(text = "Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Abbrechen")
                }
            }
        )
    }
}
