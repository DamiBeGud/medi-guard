package com.medi.guard.ui.addmedication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medi.guard.data.room.RepeatOption
import com.medi.guard.ui.components.LargePrimaryButton
import com.medi.guard.ui.components.TimePickerField
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalError
import com.medi.guard.ui.theme.ClinicalOutlineVariant
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalSurface
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant

@Composable
fun AddMedicationScreen(
    state: AddMedicationUiState,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onRepeatOptionChange: (RepeatOption) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (state.isEditMode) "Medikament bearbeiten" else "Medikament hinzufügen",
                style = MaterialTheme.typography.headlineLarge,
                color = ClinicalText
            )
            Text(
                text = if (state.isEditMode) {
                    "Aktualisieren Sie die Erinnerung."
                } else {
                    "Erfassen Sie ein neues Medikament für Ihren Plan."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalTextVariant
            )
        }

        LabeledTextField(
            label = "Name des Medikaments",
            value = state.name,
            placeholder = "z.B. Aspirin",
            onValueChange = onNameChange
        )

        LabeledTextField(
            label = "Dosierung",
            value = state.dosage,
            placeholder = "z.B. 500 mg",
            onValueChange = onDosageChange,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = ClinicalTextVariant
                )
            }
        )

        TimePickerField(
            label = "Uhrzeit der Einnahme",
            hour = state.hour,
            minute = state.minute,
            onTimeChange = onTimeChange
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Wiederholung",
                style = MaterialTheme.typography.labelLarge,
                color = ClinicalText
            )
            RepeatOptionCard(
                title = "Täglich",
                subtitle = "Jeden Tag zur gleichen Zeit",
                selected = state.repeatOption == RepeatOption.DAILY,
                onClick = { onRepeatOptionChange(RepeatOption.DAILY) }
            )
            RepeatOptionCard(
                title = "Nur heute",
                subtitle = "Einmalige Einnahme",
                selected = state.repeatOption == RepeatOption.ONCE,
                onClick = { onRepeatOptionChange(RepeatOption.ONCE) }
            )
        }

        PrivacyInfoCard()

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalError
            )
        }

        LargePrimaryButton(
            text = if (state.isSaving) "Speichern..." else "Speichern",
            icon = Icons.Filled.Check,
            onClick = onSaveClick,
            enabled = !state.isSaving
        )
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = ClinicalText)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClinicalTextVariant
                )
            },
            trailingIcon = trailingIcon,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun RepeatOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ClinicalPrimary else ClinicalOutlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ClinicalText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClinicalTextVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = ClinicalPrimary)
            )
        }
    }
}

@Composable
private fun PrivacyInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicalSuccessContainer.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.HealthAndSafety,
                contentDescription = null,
                tint = ClinicalSuccess,
                modifier = Modifier.size(28.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Warum ist das sicher?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = ClinicalSuccess
            )
            Text(
                text = "Ihre Gesundheitsdaten werden lokal auf diesem Gerät geschützt. Vor dem Entsperren bleiben Details verborgen.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalTextVariant
            )
        }
    }
}
