package com.medi.guard.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY reminderHour ASC, reminderMinute ASC, name ASC")
    fun observeMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY reminderHour ASC, reminderMinute ASC, name ASC")
    fun observeActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    fun observeMedication(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedication(id: Long): MedicationEntity?

    @Insert
    suspend fun insertMedication(entity: MedicationEntity): Long

    @Update
    suspend fun updateMedication(entity: MedicationEntity)

    @Query("UPDATE medications SET isActive = :isActive WHERE id = :id")
    suspend fun setMedicationActive(id: Long, isActive: Boolean)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)
}
