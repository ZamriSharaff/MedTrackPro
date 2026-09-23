package com.zamri.s35702753.medtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager
import com.zamri.s35702753.medtrack.viewmodel.AddMedicationViewModel
import com.zamri.s35702753.medtrack.viewmodel.HomeViewModel
import com.zamri.s35702753.medtrack.viewmodel.LoginViewModel
import com.zamri.s35702753.medtrack.viewmodel.MedCoachViewModel
import com.zamri.s35702753.medtrack.viewmodel.SettingsViewModel
import com.zamri.s35702753.medtrack.viewmodel.SignUpViewModel
import com.zamri.s35702753.medtrack.viewmodel.SymptomsViewModel
import com.zamri.s35702753.medtrack.viewmodel.ClinicianViewModel

class MedTrackViewModelFactory(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
                SignUpViewModel(repository) as T

            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(AddMedicationViewModel::class.java) ->
                AddMedicationViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(SymptomsViewModel::class.java) ->
                SymptomsViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(MedCoachViewModel::class.java) ->
                MedCoachViewModel(repository, sessionManager) as T

            modelClass.isAssignableFrom(ClinicianViewModel::class.java) ->
                ClinicianViewModel(repository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}