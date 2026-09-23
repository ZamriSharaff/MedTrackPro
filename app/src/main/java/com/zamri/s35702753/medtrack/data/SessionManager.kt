package com.zamri.s35702753.medtrack.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("MedTrackPrefs", Context.MODE_PRIVATE)

    fun getLoggedInPatientId(): String? {
        return prefs.getString("logged_in_patient_id", null)
    }

    fun setLoggedInPatientId(patientId: String) {
        prefs.edit()
            .putString("logged_in_patient_id", patientId)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove("logged_in_patient_id")
            .apply()
    }
}