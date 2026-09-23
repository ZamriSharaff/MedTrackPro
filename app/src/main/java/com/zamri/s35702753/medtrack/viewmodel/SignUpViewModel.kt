package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.PatientEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String = "",
    val isLoading: Boolean = false
)

sealed class SignUpEvent {
    data class ShowMessage(val message: String) : SignUpEvent()
    data class NavigateToLogin(val patientId: String) : SignUpEvent()
}

class SignUpViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SignUpEvent>()
    val events: SharedFlow<SignUpEvent> = _events

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, error = "")
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value, error = "")
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = "")
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = "")
    }

    fun onSignUpClick() {
        val state = _uiState.value

        viewModelScope.launch {
            val validationError = validate(state)
            if (validationError.isNotBlank()) {
                _uiState.value = state.copy(error = validationError)
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = "")

            val existing = repository.getPatientByPhone(state.phoneNumber.trim())
            if (existing != null) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Account already exists with this phone number"
                )
                return@launch
            }

            val newPatientId = repository.generateNextPatientId()

            repository.insertPatient(
                PatientEntity(
                    patientId = newPatientId,
                    phoneNumber = state.phoneNumber.trim(),
                    name = state.fullName.trim(),
                    password = state.password
                )
            )

            _uiState.value = state.copy(isLoading = false, error = "")
            _events.emit(SignUpEvent.ShowMessage("Account created successfully"))
            _events.emit(SignUpEvent.NavigateToLogin(newPatientId))
        }
    }

    private fun validate(state: SignUpUiState): String {
        if (
            state.fullName.isBlank() ||
            state.phoneNumber.isBlank() ||
            state.password.isBlank() ||
            state.confirmPassword.isBlank()
        ) {
            return "Please fill all fields"
        }

        if (!state.phoneNumber.startsWith("04")) {
            return "Phone number must start with 04"
        }

        if (state.phoneNumber.length != 10) {
            return "Phone number must be exactly 10 digits"
        }

        if (!state.phoneNumber.all { it.isDigit() }) {
            return "Phone number must only contain digits"
        }

        if (state.password.length < 8) {
            return "Password must be at least 8 characters long"
        }

        if (!state.password.any { it.isLetter() }) {
            return "Password must contain at least one letter"
        }

        if (!state.password.any { it.isDigit() }) {
            return "Password must contain at least one digit"
        }

        if (state.password != state.confirmPassword) {
            return "Passwords do not match"
        }

        return ""
    }
}