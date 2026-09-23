package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.MedicationEntity
import com.zamri.s35702753.medtrack.data.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddMedicationUiState(
    val medicationName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val medicationType: String = "",
    val notes: String = "",
    val time: String = "",
    val error: String = "",
    val isLoading: Boolean = false
)

sealed class AddMedicationEvent {
    data object NavigateHome : AddMedicationEvent()
    data class ShowSnackbar(val message: String) : AddMedicationEvent()
}

class AddMedicationViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddMedicationEvent>()
    val events: SharedFlow<AddMedicationEvent> = _events

    fun onMedicationNameChange(value: String) {
        _uiState.value = _uiState.value.copy(medicationName = value, error = "")
    }

    fun onDosageChange(value: String) {
        _uiState.value = _uiState.value.copy(dosage = value, error = "")
    }

    fun onFrequencyChange(value: String) {
        _uiState.value = _uiState.value.copy(frequency = value, error = "")
    }

    fun onMedicationTypeChange(value: String) {
        _uiState.value = _uiState.value.copy(medicationType = value, error = "")
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(time = value, error = "")
    }

    fun onClearClick() {
        _uiState.value = AddMedicationUiState()
    }

    fun onSaveClick() {
        val state = _uiState.value

        viewModelScope.launch {
            val error = validate(state)
            if (error.isNotBlank()) {
                _uiState.value = state.copy(error = error)
                return@launch
            }

            val patientId = sessionManager.getLoggedInPatientId()
            if (patientId.isNullOrBlank()) {
                _uiState.value = state.copy(error = "No active session found")
                return@launch
            }

            repository.insertMedication(
                MedicationEntity(
                    patientId = patientId,
                    medicationName = state.medicationName.trim(),
                    dosage = state.dosage.trim(),
                    frequency = state.frequency,
                    scheduledTime = state.time,
                    medicationType = state.medicationType,
                    notes = state.notes.trim()
                )
            )

            _uiState.value = AddMedicationUiState()
            _events.emit(AddMedicationEvent.ShowSnackbar("Medication added successfully"))
            _events.emit(AddMedicationEvent.NavigateHome)
        }
    }

    private fun validate(state: AddMedicationUiState): String {
        if (state.medicationName.isBlank() || state.dosage.isBlank() || state.frequency.isBlank() || state.time.isBlank() || state.medicationType.isBlank()) {
            return "Please fill all required fields"
        }

        val dosageRegex = Regex("^\\d+(\\.\\d+)?(mg|ml|g)$")
        if (!dosageRegex.matches(state.dosage.trim())) {
            return "Invalid dosage format. Use e.g. 500mg, 10ml, 2.5g"
        }

        return ""
    }
}