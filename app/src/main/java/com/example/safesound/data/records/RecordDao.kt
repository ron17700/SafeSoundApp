package com.example.safesound.data.records

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface RecordDao {
    @Query("SELECT * FROM records")
    fun getAllPublicRecords(): List<RecordEntity>

    @Query("DELETE FROM records")
    fun deleteAllPublicRecords(): Int

    @Insert
    fun insert(record: RecordEntity)

    @Insert
    fun insertAll(records: List<RecordEntity>)

    @Update
    fun update(record: RecordEntity)

    @Delete
    fun delete(record: RecordEntity)

    @Query("UPDATE records SET timestamp = :timestamp WHERE id = :id")
    fun updateCacheTimestamp(id: String, timestamp: Long): Int

    @Query("DELETE FROM records WHERE timestamp < :expiryTime")
    fun deleteExpiredRecords(expiryTime: Long): Int
}