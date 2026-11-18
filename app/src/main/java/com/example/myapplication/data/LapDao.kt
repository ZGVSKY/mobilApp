package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LapDao {
    @Insert
    suspend fun insert(lap: Lap)

    @Query("SELECT * FROM laps WHERE username = :username ORDER BY timestamp DESC")
    suspend fun getLapsForUser(username: String): List<Lap>

    @Query("DELETE FROM laps WHERE username = :username")
    suspend fun clearLapsForUser(username: String)

    @Query("DELETE FROM laps")
    suspend fun clearAllLaps()
}