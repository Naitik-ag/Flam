package com.example.flam.di

import android.content.Context
import com.example.flam.data.camera.CameraController
import com.example.flam.data.nat.NativeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideCameraController(
        @ApplicationContext context: Context
    ): CameraController = CameraController(context)

    @Provides
    @Singleton
    fun provideNativeRepository(): NativeRepository = NativeRepository()
}
