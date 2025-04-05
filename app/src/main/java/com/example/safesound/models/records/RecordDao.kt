package com.example.safesound.models.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE userId= :userId ORDER BY createdAt")
    fun getRecords(userId: String): List<RecordEntity>

    @Query("SELECT * FROM records WHERE isMyRecords = 0 ORDER BY createdAt")
    fun getPublicRecords(): List<RecordEntity>

    @Query("DELETE FROM records WHERE userId= :userId")
    fun deleteRecords(userId: String)

    @Query("DELETE FROM records WHERE isMyRecords = 0")
    fun deletePublicRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(records: List<RecordEntity>)

    @Query("DELETE FROM records WHERE timestamp < :expirationTime")
    fun evictOldEntries(expirationTime: Long)
}