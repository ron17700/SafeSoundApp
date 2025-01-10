package com.example.safesound.data.records

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.safesound.data.user.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromUser(user: User?): String? {
        return Gson().toJson(user)
    }

    @TypeConverter
    fun toUser(userString: String?): User? {
        return Gson().fromJson(userString, object : TypeToken<User>() {}.type)
    }
}

@Database(entities = [RecordEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: RecordDatabase? = null

        fun getInstance(context: Context): RecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordDatabase::class.java,
                    "records_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}