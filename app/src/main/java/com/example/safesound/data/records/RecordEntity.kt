package com.example.safesound.data.records

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.safesound.data.user.User

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: String,
    var recordClass: String,
    val public: Boolean,
    val isFavorite: Boolean,
    val userId: User?,
    val latitude: Double?,
    val longitude: Double?,
    val image: String?
)