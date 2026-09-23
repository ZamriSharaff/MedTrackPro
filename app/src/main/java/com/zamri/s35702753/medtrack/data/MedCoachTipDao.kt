package com.zamri.s35702753.medtrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: MedCoachTipEntity)

    @Query(
        """
        SELECT * FROM MedCoachTipEntity
        WHERE patientId = :patientId
        ORDER BY createdAt DESC
        """
    )
    fun getTipsByPatientId(patientId: String): Flow<List<MedCoachTipEntity>>
}