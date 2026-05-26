package com.medi.guard.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeHistoryDao {
    @Query("SELECT * FROM intake_history ORDER BY scheduledAtMillis DESC, takenAtMillis DESC")
    fun observeHistory(): Flow<List<IntakeHistoryEntity>>

    @Query(
        "SELECT * FROM intake_history " +
            "WHERE scheduledAtMillis BETWEEN :startMillis AND :endMillis " +
            "ORDER BY scheduledAtMillis ASC"
    )
    fun observeHistoryBetween(startMillis: Long, endMillis: Long): Flow<List<IntakeHistoryEntity>>

    @Query("SELECT * FROM intake_history WHERE medicationId = :medicationId ORDER BY scheduledAtMillis DESC")
    fun observeHistoryForMedication(medicationId: Long): Flow<List<IntakeHistoryEntity>>

    @Insert
    suspend fun insertHistory(entity: IntakeHistoryEntity): Long

    @Query("DELETE FROM intake_history WHERE medicationId = :medicationId")
    suspend fun deleteHistoryForMedication(medicationId: Long)
}
