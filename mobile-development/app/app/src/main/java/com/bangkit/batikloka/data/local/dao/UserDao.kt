package com.bangkit.batikloka.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bangkit.batikloka.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun checkUserExists(email: String): Int

    @Query("SELECT profile_picture FROM users WHERE email = :email")
    fun getProfilePicture(email: String): ByteArray?

    @Query("UPDATE users SET name = :newUsername WHERE email = :email")
    suspend fun updateUsername(email: String, newUsername: String)

    @Query("UPDATE users SET password = :newHashedPassword WHERE email = :email")
    suspend fun updatePassword(email: String, newHashedPassword: String)

    @Query("UPDATE users SET profile_picture = :profilePicture WHERE email = :email")
    suspend fun updateProfilePicture(email: String, profilePicture: ByteArray?)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
}