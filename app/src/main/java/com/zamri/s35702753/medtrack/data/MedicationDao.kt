package com.zamri.s35702753.medtrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Query("SELECT * FROM MedicationEntity WHERE patientId = :patientId")
    fun getMedicationsByPatientId(patientId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM MedicationEntity")
    suspend fun getAllMedications(): List<MedicationEntity>

    @Query("SELECT COUNT(*) FROM MedicationEntity")
    suspend fun getTotalMedications(): Int
}