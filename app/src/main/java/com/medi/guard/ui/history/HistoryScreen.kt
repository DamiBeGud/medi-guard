package com.medi.guard.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.medi.guard.data.room.IntakeStatus
import com.medi.guard.ui.components.ClinicalCard
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalError
import com.medi.guard.ui.theme.ClinicalErrorContainer
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalSurfaceContainer
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onQueryChange: (String) -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Verlauf",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ClinicalText
                )
                Text(
                    text = "Hier sehen Sie, wann Medikamente bestätigt wurden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClinicalTextVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                placeholder = {
                    Text(
                        text = "Medikament suchen...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (state.groups.isEmpty()) {
            item {
                EmptyHistoryCard()
            }
        } else {
            state.groups.forEach { group ->
                item {
                    Text(
                        text = group.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = ClinicalTextVariant
                    )
                }
                items(group.entries, key = { it.id }) { entry ->
                    HistoryEntryCard(entry = entry)
                }
            }
        }

        item {
            WeeklySummaryCard(text = state.weeklyText)
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    ClinicalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = ClinicalPrimary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "Noch kein Verlauf",
                style = MaterialTheme.typography.headlineMedium,
                color = ClinicalText
            )
            Text(
                text = "Bestätigte Einnahmen erscheinen hier automatisch.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalTextVariant
            )
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: HistoryEntryUi) {
    val isTaken = entry.status == IntakeStatus.TAKEN
    val icon = if (isTaken) Icons.Filled.CheckCircle else Icons.Filled.Warning
    val background = if (isTaken) ClinicalSuccessContainer.copy(alpha = 0.55f) else ClinicalErrorContainer
    val content = if (isTaken) ClinicalSuccess else ClinicalError

    ClinicalCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = entry.medicationName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClinicalText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = entry.dosage,
                        style = MaterialTheme.typography.labelLarge,
                        color = ClinicalTextVariant,
                        modifier = Modifier
                            .background(ClinicalSurfaceContainer, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Text(
                    text = entry.takenText ?: entry.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = content
                )
                Text(
                    text = entry.scheduledText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClinicalTextVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicalPrimary, RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Wochenübersicht",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(1f, 0.65f, 0.7f, 1f, 0.95f, 0.45f, 0.3f).forEachIndexed { index, heightFactor ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height((64 * heightFactor).dp)
                        .background(
                            if (index == 4) Color.White else Color.White.copy(alpha = 0.28f),
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "D", "M", "D", "F", "S", "S").forEach {
                Text(text = it, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}
