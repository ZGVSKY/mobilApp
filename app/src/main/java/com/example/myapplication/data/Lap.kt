package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laps")
data class Lap(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val lapTime: String,
    val timestamp: Long = System.currentTimeMillis()
)