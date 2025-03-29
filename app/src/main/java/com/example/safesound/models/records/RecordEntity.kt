package com.example.safesound.models.records

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.safesound.data.records.Record
import com.example.safesound.data.user.User

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey var id: String,
    var name: String,
    var createdAt: String,
    var recordClass: String,
    var isPublic: Boolean,
    var favorite: Boolean,
    var userId: String,
    var userEmail: String,
    var userName: String,
    var userRole: String,
    var userProfileImage: String,
    var latitude: Double?,
    var longitude: Double?,
    var image: String?,
    var isMyRecords: Boolean,
    var timestamp: Long = System.currentTimeMillis()
)

fun RecordEntity.toRecord(): Record {
    return Record(
        _id = this.id,
        name = this.name,
        createdAt = this.createdAt,
        recordClass = this.recordClass,
        public = this.isPublic,
        isFavorite = this.favorite,
        userId = User(
            this.userId,
            this.userName,
            this.userEmail,
            this.userRole,
            this.userProfileImage
        ),
        latitude = this.latitude,
        longitude = this.longitude,
        image = this.image
    )
}