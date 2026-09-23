package com.zamri.s35702753.medtrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TakenStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(status: TakenStatusEntity)

    @Query("""
        SELECT * FROM TakenStatusEntity 
        WHERE patientId = :patientId AND date = :date
    """)
    fun getTodayStatus(patientId: String, date: String): Flow<List<TakenStatusEntity>>

    @Query("""
        DELETE FROM TakenStatusEntity 
        WHERE patientId = :patientId AND date != :date
    """)
    suspend fun clearOldDays(patientId: String, date: String)
}