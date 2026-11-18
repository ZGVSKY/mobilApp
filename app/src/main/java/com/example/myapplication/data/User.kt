package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val role: String = "user", // "admin", "user", "guest"
    val isDarkTheme: Boolean = false,
    val locale: String = "en"
)