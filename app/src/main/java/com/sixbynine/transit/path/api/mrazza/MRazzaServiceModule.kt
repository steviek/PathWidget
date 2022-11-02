package com.sixbynine.transit.path.api.mrazza

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sixbynine.transit.path.serialization.JsonFormat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType
import retrofit2.Retrofit

@InstallIn(SingletonComponent::class)
@Module
object MRazzaServiceModule {
    @Provides
    fun provideMRazzaService(): MRazzaService {
        val contentType = MediaType.get("application/json")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://path.api.razza.dev/v1/")
            .addConverterFactory(JsonFormat.asConverterFactory(contentType))
            .build()
        return retrofit.create(MRazzaService::class.java)
    }
}
