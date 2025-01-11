package com.example.safesound.di

import android.content.Context
import com.example.safesound.data.records.RecordDatabase
import com.example.safesound.models.records.RecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): RecordDatabase {
        return RecordDatabase.getInstance(context)
    }

    @Provides
    fun provideRecordDao(database: RecordDatabase): RecordDao {
        return database.recordDao()
    }
}