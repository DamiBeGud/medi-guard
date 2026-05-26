package com.medi.guard.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medi.guard.ui.components.ClinicalCard
import com.medi.guard.ui.components.LargePrimaryButton
import com.medi.guard.ui.theme.ClinicalBackground
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalPrimarySoft
import com.medi.guard.ui.theme.ClinicalSuccessContainer
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant
import com.medi.guard.ui.theme.ClinicalWarningContainer

@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicalBackground)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(ClinicalPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Willkommen bei\nMediGuard",
                style = MaterialTheme.typography.headlineLarge,
                color = ClinicalText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Ihr einfacher und sicherer Medikamenten-Erinnerer.",
                style = MaterialTheme.typography.bodyLarge,
                color = ClinicalTextVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
            BenefitCard(
                icon = Icons.Filled.NotificationsActive,
                title = "Pünktliche Erinnerungen",
                body = "Verpassen Sie nie wieder eine Einnahme Ihrer wichtigen Medikamente.",
                iconBackground = ClinicalSuccessContainer
            )
            BenefitCard(
                icon = Icons.Filled.HealthAndSafety,
                title = "Schutz Ihrer Privatsphäre",
                body = "Ihre Gesundheitsdaten bleiben lokal auf diesem Gerät geschützt.",
                iconBackground = ClinicalWarningContainer
            )
            BenefitCard(
                icon = Icons.Filled.RestartAlt,
                title = "Funktioniert nach Neustart",
                body = "Sichere Alarme bleiben auch nach einem Handy-Neustart aktiv.",
                iconBackground = ClinicalPrimarySoft
            )
        }

        LargePrimaryButton(
            text = "Loslegen",
            icon = Icons.Filled.ArrowForward,
            onClick = onStartClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Keine Registrierung erforderlich.",
            style = MaterialTheme.typography.labelLarge,
            color = ClinicalTextVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BenefitCard(
    icon: ImageVector,
    title: String,
    body: String,
    iconBackground: Color
) {
    ClinicalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = ClinicalText
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
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
}
