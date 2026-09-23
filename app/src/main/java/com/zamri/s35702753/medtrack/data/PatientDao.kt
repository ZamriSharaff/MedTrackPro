package com.zamri.s35702753.medtrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Query("SELECT * FROM PatientEntity WHERE patientId = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: String): PatientEntity?

    @Query("SELECT * FROM PatientEntity WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getPatientByPhone(phoneNumber: String): PatientEntity?

    @Query("UPDATE PatientEntity SET password = :password WHERE patientId = :patientId")
    suspend fun updatePassword(patientId: String, password: String)

    @Query("SELECT * FROM PatientEntity")
    suspend fun getAllPatients(): List<PatientEntity>

    @Query("SELECT COUNT(*) FROM PatientEntity")
    suspend fun getTotalPatients(): Int
}