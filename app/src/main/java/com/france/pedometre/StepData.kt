package com.france.pedometre

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps_table")
data class StepData(
    @PrimaryKey val date: String, // Format "yyyy-MM-dd"
    val steps: Int
)