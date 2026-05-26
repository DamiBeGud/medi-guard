package com.medi.guard.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medi.guard.MediGuardApplication
import com.medi.guard.ui.AddMedicationViewModelFactory
import com.medi.guard.ui.HistoryViewModelFactory
import com.medi.guard.ui.MedicationDetailViewModelFactory
import com.medi.guard.ui.TodayViewModelFactory
import com.medi.guard.ui.addmedication.AddMedicationScreen
import com.medi.guard.ui.addmedication.AddMedicationViewModel
import com.medi.guard.ui.detail.MedicationDetailScreen
import com.medi.guard.ui.detail.MedicationDetailViewModel
import com.medi.guard.ui.detail.SafetyInfoScreen
import com.medi.guard.ui.history.HistoryScreen
import com.medi.guard.ui.history.HistoryViewModel
import com.medi.guard.ui.theme.ClinicalPrimary
import com.medi.guard.ui.theme.ClinicalSurface
import com.medi.guard.ui.theme.ClinicalText
import com.medi.guard.ui.theme.ClinicalTextVariant
import com.medi.guard.ui.today.TodayScreen
import com.medi.guard.ui.today.TodayViewModel

object MediGuardRoutes {
    const val Today = "today"
    const val AddMedication = "add-medication"
    const val History = "history"
    const val SafetyInfo = "safety-info"
    const val DetailPattern = "medication-detail/{medicationId}"
    const val EditPattern = "edit-medication/{medicationId}"

    fun detail(medicationId: Long): String = "medication-detail/$medicationId"
    fun edit(medicationId: Long): String = "edit-medication/$medicationId"
}

@Composable
fun MediGuardMainApp(app: MediGuardApplication) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            MediGuardTopBar(
                currentRoute = currentRoute,
                onBackClick = { navController.popBackStack() },
                onSafetyClick = { navController.navigate(MediGuardRoutes.SafetyInfo) }
            )
        },
        bottomBar = {
            MediGuardBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MediGuardRoutes.Today,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MediGuardRoutes.Today) {
                val viewModel: TodayViewModel = viewModel(
                    factory = TodayViewModelFactory(app.medicationRepository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.message) {
                    state.message?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.consumeMessage()
                    }
                }
                TodayScreen(
                    state = state,
                    onMedicationTaken = viewModel::markTaken,
                    onSnoozeMedication = viewModel::snooze,
                    onAddMedicationClick = { navController.navigate(MediGuardRoutes.AddMedication) },
                    onMedicationClick = { navController.navigate(MediGuardRoutes.detail(it)) }
                )
            }

            composable(MediGuardRoutes.AddMedication) {
                val viewModel: AddMedicationViewModel = viewModel(
                    key = "add_new",
                    factory = AddMedicationViewModelFactory(app.medicationRepository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.successMessage) {
                    state.successMessage?.let {
                        viewModel.consumeSuccess()
                        navController.navigate(MediGuardRoutes.Today) {
                            launchSingleTop = true
                            popUpTo(MediGuardRoutes.Today)
                        }
                        snackbarHostState.showSnackbar(it)
                    }
                }
                AddMedicationScreen(
                    state = state,
                    onNameChange = viewModel::updateName,
                    onDosageChange = viewModel::updateDosage,
                    onTimeChange = viewModel::updateTime,
                    onRepeatOptionChange = viewModel::updateRepeatOption,
                    onSaveClick = viewModel::save
                )
            }

            composable(MediGuardRoutes.History) {
                val viewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(app.medicationRepository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                HistoryScreen(
                    state = state,
                    onQueryChange = viewModel::updateQuery
                )
            }

            composable(MediGuardRoutes.SafetyInfo) {
                SafetyInfoScreen()
            }

            composable(
                route = MediGuardRoutes.DetailPattern,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) { entry ->
                val medicationId = entry.arguments?.getLong("medicationId") ?: return@composable
                val viewModel: MedicationDetailViewModel = viewModel(
                    key = "detail_$medicationId",
                    factory = MedicationDetailViewModelFactory(app.medicationRepository, medicationId)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.message) {
                    state.message?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.consumeMessage()
                    }
                }
                LaunchedEffect(state.deleted) {
                    if (state.deleted) {
                        navController.navigate(MediGuardRoutes.Today) {
                            popUpTo(MediGuardRoutes.Today)
                            launchSingleTop = true
                        }
                        snackbarHostState.showSnackbar("Medikament gelöscht")
                    }
                }
                MedicationDetailScreen(
                    state = state,
                    onTakenClick = viewModel::markTaken,
                    onSnoozeClick = viewModel::snooze,
                    onEditClick = { navController.navigate(MediGuardRoutes.edit(medicationId)) },
                    onPauseClick = viewModel::togglePaused,
                    onDeleteConfirm = viewModel::deleteMedication
                )
            }

            composable(
                route = MediGuardRoutes.EditPattern,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) { entry ->
                val medicationId = entry.arguments?.getLong("medicationId") ?: return@composable
                val viewModel: AddMedicationViewModel = viewModel(
                    key = "edit_$medicationId",
                    factory = AddMedicationViewModelFactory(app.medicationRepository, medicationId)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.successMessage) {
                    state.successMessage?.let {
                        viewModel.consumeSuccess()
                        navController.navigate(MediGuardRoutes.detail(medicationId)) {
                            popUpTo(MediGuardRoutes.DetailPattern) { inclusive = true }
                            launchSingleTop = true
                        }
                        snackbarHostState.showSnackbar(it)
                    }
                }
                AddMedicationScreen(
                    state = state,
                    onNameChange = viewModel::updateName,
                    onDosageChange = viewModel::updateDosage,
                    onTimeChange = viewModel::updateTime,
                    onRepeatOptionChange = viewModel::updateRepeatOption,
                    onSaveClick = viewModel::save
                )
            }
        }
    }
}

@Composable
private fun MediGuardTopBar(
    currentRoute: String?,
    onBackClick: () -> Unit,
    onSafetyClick: () -> Unit
) {
    val isDetail = currentRoute == MediGuardRoutes.DetailPattern
    val isEdit = currentRoute == MediGuardRoutes.EditPattern
    val title = when {
        isDetail -> "Details"
        isEdit -> "Bearbeiten"
        currentRoute == MediGuardRoutes.SafetyInfo -> "Sicherheit"
        else -> "MediGuard"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ClinicalSurface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDetail || isEdit || currentRoute == MediGuardRoutes.SafetyInfo) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = ClinicalPrimary
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.MedicalServices,
                        contentDescription = null,
                        tint = ClinicalPrimary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClinicalPrimary
                )
            }
            IconButton(onClick = onSafetyClick) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Sicherheit",
                    tint = ClinicalTextVariant
                )
            }
        }
    }
}

@Composable
private fun MediGuardBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomItem(MediGuardRoutes.Today, "Heute", Icons.Filled.CalendarToday),
        BottomItem(MediGuardRoutes.AddMedication, "Hinzufügen", Icons.Filled.AddCircle),
        BottomItem(MediGuardRoutes.History, "Verlauf", Icons.Filled.History)
    )
    val selectedRoute = when (currentRoute) {
        MediGuardRoutes.Today -> MediGuardRoutes.Today
        MediGuardRoutes.AddMedication, MediGuardRoutes.EditPattern -> MediGuardRoutes.AddMedication
        MediGuardRoutes.History -> MediGuardRoutes.History
        else -> null
    }

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicalSurface),
        containerColor = ClinicalSurface,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = null)
                },
                label = {
                    Text(text = item.label, style = MaterialTheme.typography.labelLarge)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = ClinicalPrimary,
                    unselectedIconColor = ClinicalText,
                    unselectedTextColor = ClinicalText
                )
            )
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
