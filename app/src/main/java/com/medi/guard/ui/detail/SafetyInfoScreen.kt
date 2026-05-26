package com.medi.guard.ui.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalOutlineVariant
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSuccess
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalSurface
import com.medi.guard.ui.theme.ClinicalSurfaceContainer
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant

@Composable
fun SafetyInfoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SafetyPanel()
        NotificationPreview(
            title = "Vorschau: Gesperrt",
            icon = Icons.Filled.Lock,
            body = "Zeit für Ihre Medikamente!",
            highlighted = false
        )
        NotificationPreview(
            title = "Vorschau: Entsperrt",
            icon = Icons.Filled.Medication,
            body = "Bitte nehmen Sie jetzt 20 mg Blutdrucktablette ein.",
            highlighted = true
        )
    }
}

@Composable
private fun SafetyPanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
        border = BorderStroke(2.dp, ClinicalPrimary)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ClinicalPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column {
                    Text(
                        text = "Sichere Erinnerungen",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClinicalText
                    )
                    Text(
                        text = "AKTIV",
                        modifier = Modifier
                            .background(ClinicalSuccessContainer, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = ClinicalSuccess
                    )
                }
            }

            SafetyFeature(
                icon = Icons.Filled.RestartAlt,
                title = "Nach Neustart aktiv",
                body = "Ihre Sicherheitseinstellungen bleiben auch nach einem Geräte-Neustart bestehen."
            )
            SafetyFeature(
                icon = Icons.Filled.PrivacyTip,
                title = "Privatsphäre geschützt",
                body = "Personenbezogene Daten werden im Sperrbildschirm verborgen."
            )
            SafetyFeature(
                icon = Icons.Filled.Security,
                title = "Details nach Entsperren",
                body = "Genaue Medikamenten-Namen werden erst nach Authentifizierung angezeigt."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sichere Erinnerungen aktiv",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClinicalText
                )
                Switch(checked = true, onCheckedChange = null)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = ClinicalSuccess)
                Text(
                    text = "Neustart erkannt",
                    style = MaterialTheme.typography.labelLarge,
                    color = ClinicalSuccess
                )
            }
        }
    }
}

@Composable
private fun SafetyFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicalSurfaceContainer, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ClinicalPrimary)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ClinicalText
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalTextVariant
            )
        }
    }
}

@Composable
private fun NotificationPreview(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    body: String,
    highlighted: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = ClinicalTextVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalSurface),
            border = if (highlighted) BorderStroke(2.dp, ClinicalOutlineVariant) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ClinicalPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MediGuard Erinnerung",
                            style = MaterialTheme.typography.labelLarge,
                            color = ClinicalText
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClinicalText
                        )
                    }
                    Text(
                        text = "Jetzt",
                        style = MaterialTheme.typography.labelLarge,
                        color = ClinicalTextVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewAction(text = "Später erinnern", modifier = Modifier.weight(1f))
                    PreviewAction(
                        text = "Eingenommen",
                        modifier = Modifier.weight(1f),
                        primary = true
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewAction(
    text: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {
    Box(
        modifier = modifier
            .background(
                if (primary) ClinicalPrimary else ClinicalSurfaceContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) Color.White else ClinicalPrimary
        )
    }
}
