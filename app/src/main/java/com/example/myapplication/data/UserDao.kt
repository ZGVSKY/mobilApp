package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("UPDATE users SET isDarkTheme = :isDark WHERE username = :username")
    suspend fun updateTheme(username: String, isDark: Boolean)

    @Query("UPDATE users SET locale = :locale WHERE username = :username")
    suspend fun updateLocale(username: String, locale: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}