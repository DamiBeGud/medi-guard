package com.medi.guard.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MedicationEntity::class,
        IntakeHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(MediGuardConverters::class)
abstract class MediGuardDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeHistoryDao(): IntakeHistoryDao

    companion object {
        @Volatile
        private var instance: MediGuardDatabase? = null

        fun getInstance(context: Context): MediGuardDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MediGuardDatabase::class.java,
                    "mediguard.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
