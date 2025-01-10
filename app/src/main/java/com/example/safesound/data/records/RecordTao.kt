package com.example.safesound.data.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.safesound.data.records.RecordEntity

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: RecordEntity)

    @Query("SELECT * FROM records WHERE public = 1")
    suspend fun getAllPublicRecords(): List<RecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<RecordEntity>)

    @Query("DELETE FROM records WHERE public = 1")
    suspend fun deleteAllPublicRecords()
}