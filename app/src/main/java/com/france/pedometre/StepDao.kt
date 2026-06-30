package com.france.pedometre

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@androidx.room.Dao
interface StepDao {
    @androidx.room.Query("SELECT * FROM steps_table WHERE date = :todayDate")
    suspend fun getStepsForDate(todayDate: String): StepData?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stepData: StepData)

    @androidx.room.Query("SELECT * FROM steps_table ORDER BY date DESC")
    fun getAllSteps(): Flow<List<StepData>>

    @androidx.room.Query("DELETE FROM steps_table WHERE date < :cutoffDate")
    suspend fun deleteStepsOlderThan(cutoffDate: String)
}