package com.medi.guard.ui.addmedication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.RepeatOption
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMedicationUiState(
    val name: String = "",
    val medicationType: String = "Tablette",
    val medicationTypeCustom: String = "",
    val dosageAmount: String = "",
    val dosageUnit: String = "mg",
    val dosageUnitCustom: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val repeatOption: RepeatOption = RepeatOption.DAILY,
    val reminderDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val savedMedicationId: Long? = null
)

class AddMedicationViewModel(
    private val repository: MedicationRepository,
    private val medicationId: Long? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AddMedicationUiState(isEditMode = medicationId != null)
    )
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    init {
        if (medicationId != null) {
            viewModelScope.launch {
                repository.getMedication(medicationId)?.let { medication ->
                    _uiState.update {
                        val medicationTypeIsCommon = medication.medicationType in COMMON_MEDICATION_TYPES
                        val dosageUnitIsCommon = medication.dosageUnit in COMMON_DOSAGE_UNITS
                        it.copy(
                            name = medication.name,
                            medicationType = if (medicationTypeIsCommon) {
                                medication.medicationType
                            } else {
                                OTHER_OPTION
                            },
                            medicationTypeCustom = if (medicationTypeIsCommon) {
                                ""
                            } else {
                                medication.medicationType
                            },
                            dosageAmount = medication.dosageAmount,
                            dosageUnit = if (dosageUnitIsCommon) {
                                medication.dosageUnit
                            } else {
                                OTHER_OPTION
                            },
                            dosageUnitCustom = if (dosageUnitIsCommon) {
                                ""
                            } else {
                                medication.dosageUnit
                            },
                            hour = medication.reminderHour,
                            minute = medication.reminderMinute,
                            repeatOption = medication.repeatOption,
                            reminderDayOfWeek = medication.reminderDayOfWeek
                                ?: Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateDosage(value: String) {
        _uiState.update { it.copy(dosageAmount = value, errorMessage = null) }
    }

    fun updateMedicationType(value: String) {
        _uiState.update {
            it.copy(
                medicationType = value,
                medicationTypeCustom = if (value == OTHER_OPTION) it.medicationTypeCustom else "",
                errorMessage = null
            )
        }
    }

    fun updateDosageUnit(value: String) {
        _uiState.update {
            it.copy(
                dosageUnit = value,
                dosageUnitCustom = if (value == OTHER_OPTION) it.dosageUnitCustom else "",
                errorMessage = null
            )
        }
    }

    fun updateMedicationTypeCustom(value: String) {
        _uiState.update { it.copy(medicationTypeCustom = value, errorMessage = null) }
    }

    fun updateDosageUnitCustom(value: String) {
        _uiState.update { it.copy(dosageUnitCustom = value, errorMessage = null) }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateRepeatOption(repeatOption: RepeatOption) {
        _uiState.update { it.copy(repeatOption = repeatOption) }
    }

    fun updateReminderDayOfWeek(dayOfWeek: Int) {
        _uiState.update { it.copy(reminderDayOfWeek = dayOfWeek) }
    }

    // Validates the form, resolves dropdown/custom values, and persists either a new or edited medication.
    fun save() {
        val state = _uiState.value
        val resolvedMedicationType = state.resolvedMedicationType()
        val resolvedDosageUnit = state.resolvedDosageUnit()
        if (
            state.name.isBlank() ||
            resolvedMedicationType.isBlank() ||
            state.dosageAmount.isBlank() ||
            resolvedDosageUnit.isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Bitte Name, Art, Dosierung und Einheit ausfüllen.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val savedId = if (medicationId == null) {
                    repository.addMedication(
                        name = state.name,
                        medicationType = resolvedMedicationType,
                        dosageAmount = state.dosageAmount,
                        dosageUnit = resolvedDosageUnit,
                        hour = state.hour,
                        minute = state.minute,
                        repeatOption = state.repeatOption,
                        reminderDayOfWeek = state.reminderDayOfWeek.takeIf {
                            state.repeatOption == RepeatOption.WEEKLY
                        }
                    )
                } else {
                    val updated = repository.updateMedication(
                        medicationId = medicationId,
                        name = state.name,
                        medicationType = resolvedMedicationType,
                        dosageAmount = state.dosageAmount,
                        dosageUnit = resolvedDosageUnit,
                        hour = state.hour,
                        minute = state.minute,
                        repeatOption = state.repeatOption,
                        reminderDayOfWeek = state.reminderDayOfWeek.takeIf {
                            state.repeatOption == RepeatOption.WEEKLY
                        }
                    )
                    if (!updated) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Medikament konnte nicht aktualisiert werden."
                            )
                        }
                        return@launch
                    }
                    medicationId
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = if (state.isEditMode) {
                            "Änderungen gespeichert"
                        } else {
                            "Medikament gespeichert"
                        },
                        savedMedicationId = savedId,
                        name = if (state.isEditMode) it.name else "",
                        medicationType = if (state.isEditMode) it.medicationType else "Tablette",
                        medicationTypeCustom = if (state.isEditMode) it.medicationTypeCustom else "",
                        dosageAmount = if (state.isEditMode) it.dosageAmount else "",
                        dosageUnit = if (state.isEditMode) it.dosageUnit else "mg",
                        dosageUnitCustom = if (state.isEditMode) it.dosageUnitCustom else ""
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Speichern fehlgeschlagen."
                    )
                }
            }
        }
    }

    // Clears the one-time success event after navigation or a snackbar consumed it.
    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null, savedMedicationId = null) }
    }

    // Turns the "Andere" dropdown option into the custom medication type text entered by the user.
    private fun AddMedicationUiState.resolvedMedicationType(): String {
        return if (medicationType == OTHER_OPTION) medicationTypeCustom.trim() else medicationType.trim()
    }

    // Turns the "Andere" dropdown option into the custom dosage unit text entered by the user.
    private fun AddMedicationUiState.resolvedDosageUnit(): String {
        return if (dosageUnit == OTHER_OPTION) dosageUnitCustom.trim() else dosageUnit.trim()
    }

    companion object {
        const val OTHER_OPTION = "Andere"

        val COMMON_MEDICATION_TYPES = listOf(
            "Tablette",
            "Kapsel",
            "Sirup",
            "Tropfen",
            "Salbe",
            "Spray",
            "Injektion",
            OTHER_OPTION
        )

        val COMMON_DOSAGE_UNITS = listOf(
            "mg",
            "ml",
            "Stück",
            "Tropfen",
            "Hub",
            "g",
            OTHER_OPTION
        )
    }
}
