package com.medi.guard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.medi.guard.ui.theme.ClinicalError
import com.medi.guard.ui.theme.ClinicalErrorContainer
import com.medi.guard.ui.theme.ClinicalOutlineVariant
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalSurface
import com.medi.guard.ui.theme.ClinicalWarning
import com.medi.guard.ui.theme.ClinicalWarningContainer
import com.medi.guard.ui.today.ReminderStatus

@Composable
fun ClinicalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
        border = BorderStroke(1.dp, ClinicalOutlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun StatusChip(status: ReminderStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        ReminderStatus.PENDING -> "Ausstehend"
        ReminderStatus.TAKEN -> "Eingenommen"
        ReminderStatus.MISSED -> "Verpasst"
    }
    val colors = when (status) {
        ReminderStatus.PENDING -> ClinicalWarningContainer to ClinicalWarning
        ReminderStatus.TAKEN -> ClinicalSuccessContainer.copy(alpha = 0.45f) to ClinicalSuccess
        ReminderStatus.MISSED -> ClinicalErrorContainer to ClinicalError
    }

    Text(
        text = label,
        modifier = modifier
            .background(colors.first, RoundedCornerShape(999.dp))
            .border(1.dp, colors.second.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = colors.second,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun IconLabelChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}
