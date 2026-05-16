package com.soundwave.app.core.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import com.soundwave.app.BuildConfig
import com.soundwave.app.core.cache.AudioCacheManager
import com.soundwave.app.data.local.database.SoundWaveDatabase
import com.soundwave.app.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoundWaveDatabase {
        return Room.databaseBuilder(context, SoundWaveDatabase::class.java, "soundwave.db")
            .fallbackToDestructiveMigration()
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides fun provideTrackDao(db: SoundWaveDatabase) = db.trackDao()
    @Provides fun provideAlbumDao(db: SoundWaveDatabase) = db.albumDao()
    @Provides fun provideArtistDao(db: SoundWaveDatabase) = db.artistDao()
    @Provides fun providePlaylistDao(db: SoundWaveDatabase) = db.playlistDao()
    @Provides fun provideDownloadDao(db: SoundWaveDatabase) = db.downloadDao()
    @Provides fun provideQueueDao(db: SoundWaveDatabase) = db.queueDao()
    @Provides fun provideSearchHistoryDao(db: SoundWaveDatabase) = db.searchHistoryDao()
    @Provides fun provideRecentlyPlayedDao(db: SoundWaveDatabase) = db.recentlyPlayedDao()
    @Provides fun provideLyricsDao(db: SoundWaveDatabase) = db.lyricsDao()

    @UnstableApi
    @Provides
    @Singleton
    fun provideAudioCacheManager(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): AudioCacheManager = AudioCacheManager(context, okHttpClient)
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val httpCache = Cache(cacheDir, 50 * 1024 * 1024L) // 50MB HTTP cache

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_PROXY_LOGS)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .cache(httpCache)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "SoundWave/1.0 (Android)")
                    .addHeader("Accept-Language", "en-US,en;q=0.9")
                    .build()
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                // Cache-control for API responses
                val response = chain.proceed(chain.request())
                val cacheControl = response.header("Cache-Control")
                if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("must-revalidate")) {
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=300")
                        .build()
                } else response
            }
            .build()
    }

    @Provides
    @Singleton
    @Named("jiosaavn")
    fun provideJioSaavnRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.JIOSAAVN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("proxy")
    fun provideProxyRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PROXY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("lyrics")
    fun provideLyricsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.LYRICS_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("audius")
    fun provideAudiusRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AUDIUS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideJioSaavnApi(@Named("jiosaavn") retrofit: Retrofit): JioSaavnApi =
        retrofit.create(JioSaavnApi::class.java)

    @Provides
    @Singleton
    fun provideProxyApi(@Named("proxy") retrofit: Retrofit): ProxyApi =
        retrofit.create(ProxyApi::class.java)

    @Provides
    @Singleton
    fun provideLyricsApi(@Named("lyrics") retrofit: Retrofit): LyricsApi =
        retrofit.create(LyricsApi::class.java)

    @Provides
    @Singleton
    fun provideAudiusApi(@Named("audius") retrofit: Retrofit): AudiusApi =
        retrofit.create(AudiusApi::class.java)
}
