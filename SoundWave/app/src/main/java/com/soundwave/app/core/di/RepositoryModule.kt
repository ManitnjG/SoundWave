package com.soundwave.app.core.di

import com.google.gson.Gson
import com.soundwave.app.data.repository.MusicRepositoryImpl
import com.soundwave.app.data.repository.UserPreferencesRepositoryImpl
import com.soundwave.app.domain.repository.MusicRepository
import com.soundwave.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides @Singleton
    fun provideGson(): Gson = Gson()
}
