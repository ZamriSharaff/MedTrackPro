package com.zamri.s35702753.medtrack.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object DatabaseSeeder {

    // for migration only
    private data class LegacyUser(
        val patientID: String,
        val fullName: String,
        val phoneNumber: String,
        val password: String
    )

    private data class LegacyMedication(
        val patientID: String,
        val name: String,
        val dosage: String,
        val frequency: String,
        val scheduledTime: String,
        val notes: String = ""
    )

    suspend fun seedDatabase(context: Context, repo: MedTrackRepository) {
        val prefs = context.getSharedPreferences("MedTrackPrefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("db_seeded", false)) return

        withContext(Dispatchers.IO) {
            val gson = Gson()

            val patients = loadPatientsFromCsv(context).toMutableList()
            val medications = loadMedicationsFromCsv(context).toMutableList()
            val symptoms = loadSymptomsFromCsv(context).toMutableList()

            // Migrating SignUp users from SharedPreferences to PatientEntity
            val usersJson = prefs.getString("users", null)
            if (!usersJson.isNullOrBlank()) {
                val userType = object : TypeToken<MutableList<LegacyUser>>() {}.type
                val users: MutableList<LegacyUser> = gson.fromJson(usersJson, userType)

                users.forEach { user ->
                    patients.add(
                        PatientEntity(
                            patientId = user.patientID,
                            phoneNumber = user.phoneNumber,
                            name = user.fullName,
                            password = user.password
                        )
                    )
                }
            }

            // Migrate user added medications from SharedPreferences to MedicationEntity
            val medsJson = prefs.getString("medications", null)
            if (!medsJson.isNullOrBlank()) {
                val medType = object : TypeToken<MutableList<LegacyMedication>>() {}.type
                val legacyMeds: MutableList<LegacyMedication> = gson.fromJson(medsJson, medType)

                legacyMeds.forEach { med ->
                    medications.add(
                        MedicationEntity(
                            patientId = med.patientID,
                            medicationName = med.name,
                            dosage = med.dosage,
                            frequency = med.frequency,
                            scheduledTime = med.scheduledTime,
                            medicationType = "",
                            notes = med.notes
                        )
                    )
                }
            }

            repo.insertPatients(patients.distinctBy { it.patientId })
            repo.insertMedications(
                medications.distinctBy {
                    it.patientId + it.medicationName + it.dosage + it.frequency + it.scheduledTime + it.notes
                }
            )
            repo.insertSymptoms(
                symptoms.distinctBy {
                    it.patientId + it.category + it.severity + it.notes + it.dateTime
                }
            )

            prefs.edit()
                .remove("users")
                .remove("medications")
                .putBoolean("db_seeded", true)
                .apply()
        }
    }

    private fun loadPatientsFromCsv(context: Context): List<PatientEntity> {
        val list = mutableListOf<PatientEntity>()
        val inputStream = context.assets.open("patients.csv")

        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.drop(1).forEach { line ->
                val values = line.split(",")
                if (values.size >= 4) {
                    list.add(
                        PatientEntity(
                            patientId = values[0].trim(),
                            phoneNumber = values[1].trim(),
                            name = values[2].trim(),
                            password = ""
                        )
                    )
                }
            }
        }

        return list
    }

    private fun loadMedicationsFromCsv(context: Context): List<MedicationEntity> {
        val list = mutableListOf<MedicationEntity>()
        val inputStream = context.assets.open("medications.csv")

        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.drop(1).forEach { line ->
                val values = line.split(",")
                if (values.size >= 7) {
                    list.add(
                        MedicationEntity(
                            patientId = values[0].trim(),
                            medicationName = values[1].trim(),
                            dosage = values[2].trim(),
                            frequency = values[3].trim(),
                            scheduledTime = values[4].trim(),
                            medicationType = values[5].trim(),
                            notes = values[6].trim()
                        )
                    )
                }
            }
        }

        return list
    }

    private fun loadSymptomsFromCsv(context: Context): List<SymptomEntity> {
        val list = mutableListOf<SymptomEntity>()
        val inputStream = context.assets.open("symptoms.csv")

        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.drop(1).forEach { line ->
                val values = line.split(",")
                if (values.size >= 5) {
                    list.add(
                        SymptomEntity(
                            patientId = values[0].trim(),
                            category = values[1].trim(),
                            severity = values[2].trim().toIntOrNull() ?: 0,
                            notes = values[3].trim(),
                            dateTime = values[4].trim()
                        )
                    )
                }
            }
        }

        return list
    }
}