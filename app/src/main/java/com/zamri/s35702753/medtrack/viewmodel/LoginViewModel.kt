package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val patientId: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isClaimMode: Boolean = true,
    val error: String = "",
    val isLoading: Boolean = false
)

sealed class LoginEvent {
    data object NavigateHome : LoginEvent()
}

class LoginViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    fun loadLoginMode(patientIdFromSignup: String?, startInClaimMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            patientId = patientIdFromSignup.orEmpty(),
            isClaimMode = startInClaimMode,
            error = ""
        )
    }

    fun onPatientIdChange(value: String) {
        _uiState.value = _uiState.value.copy(patientId = value, error = "")
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

    fun setMode(isClaimMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            isClaimMode = isClaimMode,
            error = ""
        )
    }

    fun onPrimaryActionClick() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = "")

            val result = if (state.isClaimMode) {
                claimAccount(
                    patientId = state.patientId,
                    phoneNumber = state.phoneNumber,
                    newPassword = state.password,
                    confirmPassword = state.confirmPassword
                )
            } else {
                loginExisting(
                    patientId = state.patientId,
                    password = state.password
                )
            }

            if (result == "SUCCESS") {
                sessionManager.setLoggedInPatientId(state.patientId.trim())
                _uiState.value = state.copy(isLoading = false, error = "")
                _events.emit(LoginEvent.NavigateHome)
            } else {
                _uiState.value = state.copy(isLoading = false, error = result)
            }
        }
    }

    private suspend fun claimAccount(
        patientId: String,
        phoneNumber: String,
        newPassword: String,
        confirmPassword: String
    ): String {
        if (patientId.isBlank() || phoneNumber.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return "Please fill all fields"
        }

        if (newPassword != confirmPassword) {
            return "Passwords do not match"
        }

        val passwordValidation = validatePasswordRules(newPassword)
        if (passwordValidation.isNotBlank()) {
            return passwordValidation
        }

        val patient = repository.getPatientById(patientId.trim())
            ?: return "No account found with this Patient ID"

        if (patient.phoneNumber != phoneNumber.trim()) {
            return "Phone number does not match"
        }

        if (patient.password.isNotBlank()) {
            return "This account has already been claimed"
        }

        repository.updatePassword(patientId.trim(), newPassword)
        return "SUCCESS"
    }

    private suspend fun loginExisting(
        patientId: String,
        password: String
    ): String {
        if (patientId.isBlank() || password.isBlank()) {
            return "Please fill all fields"
        }

        val patient = repository.getPatientById(patientId.trim())
            ?: return "No account found with this Patient ID"

        if (patient.password.isBlank()) {
            return "This account has not been claimed yet"
        }

        return if (patient.password == password) {
            "SUCCESS"
        } else {
            "Incorrect password"
        }
    }

    private fun validatePasswordRules(password: String): String {
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }

        if (!password.any { it.isLetter() }) {
            return "Password must contain at least one letter"
        }

        if (!password.any { it.isDigit() }) {
            return "Password must contain at least one digit"
        }

        return ""
    }
}