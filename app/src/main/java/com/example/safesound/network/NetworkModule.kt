package com.example.safesound.network

import com.example.safesound.data.auth.AuthApiService
import com.example.safesound.data.records.RecordsApiService
import com.example.safesound.data.user.UsersApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val BASE_URL = "http://10.0.2.2:3001/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("unauthRetrofit")
    fun provideUnauthRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("appRetrofit")
    fun provideAppRetrofit(
        @Named("unauthRetrofit") unauthRetrofit: Retrofit,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return unauthRetrofit.newBuilder()
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("unsecureAuthApi")
    fun provideUnsecureAuthApi(
        @Named("unauthRetrofit") retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("authApi")
    fun provideAuthApi(
        @Named("appRetrofit") retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("recordsApi")
    fun provideRecordsApi(
        @Named("appRetrofit") retrofit: Retrofit
    ): RecordsApiService {
        return retrofit.create(RecordsApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("usersApi")
    fun provideUsersApi(
        @Named("appRetrofit") retrofit: Retrofit
    ): UsersApiService {
        return retrofit.create(UsersApiService::class.java)
    }
}
