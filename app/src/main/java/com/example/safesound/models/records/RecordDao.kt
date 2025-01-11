package com.example.safesound.models.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE NOT (public = :isMyRecords)")
    fun getRecordsByType(isMyRecords: Boolean): List<RecordEntity>

    @Query("DELETE FROM records WHERE NOT (public = :isMyRecords)")
    fun deleteRecordsByType(isMyRecords: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(records: List<RecordEntity>)

    @Query("UPDATE records SET timestamp = :timestamp WHERE id = :id AND NOT (public = :isMyRecords)")
    fun updateCacheTimestampByType(id: String, timestamp: Long, isMyRecords: Boolean): Int
}