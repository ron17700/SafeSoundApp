package com.example.safesound.data.records

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey var id: String,
    var name: String,
    var createdAt: String,
    var recordClass: String,
    var public: Boolean,
    var favorite: Boolean,
    var userId: String,
    var latitude: Double?,
    var longitude: Double?,
    var image: String?,
    var timestamp: Long?
) {
    constructor()
            : this("", "", "", "", false, false, "", null, null, null, null)
}

