package com.medi.guard.ui.addmedication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medi.guard.data.repository.MedicationRepository
import com.medi.guard.data.room.RepeatOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMedicationUiState(
    val name: String = "",
    val dosage: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val repeatOption: RepeatOption = RepeatOption.DAILY,
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
                        it.copy(
                            name = medication.name,
                            dosage = medication.dosage,
                            hour = medication.reminderHour,
                            minute = medication.reminderMinute,
                            repeatOption = medication.repeatOption
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
        _uiState.update { it.copy(dosage = value, errorMessage = null) }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateRepeatOption(repeatOption: RepeatOption) {
        _uiState.update { it.copy(repeatOption = repeatOption) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.dosage.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Bitte Name und Dosierung ausfüllen.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val savedId = if (medicationId == null) {
                repository.addMedication(
                    name = state.name,
                    dosage = state.dosage,
                    hour = state.hour,
                    minute = state.minute,
                    repeatOption = state.repeatOption
                )
            } else {
                repository.updateMedication(
                    medicationId = medicationId,
                    name = state.name,
                    dosage = state.dosage,
                    hour = state.hour,
                    minute = state.minute,
                    repeatOption = state.repeatOption
                )
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
                    dosage = if (state.isEditMode) it.dosage else ""
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null, savedMedicationId = null) }
    }
}
