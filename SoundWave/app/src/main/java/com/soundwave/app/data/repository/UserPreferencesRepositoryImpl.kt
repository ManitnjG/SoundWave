package com.soundwave.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.soundwave.app.domain.model.*
import com.soundwave.app.domain.repository.UserPreferencesRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : UserPreferencesRepository {

    private object Keys {
        val STREAMING_QUALITY = stringPreferencesKey("streaming_quality")
        val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val NORMALIZATION = booleanPreferencesKey("normalization")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_BANDS = stringPreferencesKey("equalizer_bands")
        val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
        val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("download_wifi_only")
        val AUTO_DOWNLOAD = booleanPreferencesKey("auto_download")
        val DOWNLOAD_PATH = stringPreferencesKey("download_path")
        val CACHE_SIZE = longPreferencesKey("cache_size")
        val LYRICS_ENABLED = booleanPreferencesKey("lyrics_enabled")
        val FLOATING_LYRICS = booleanPreferencesKey("floating_lyrics")
        val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
        val VISUALIZER_STYLE = stringPreferencesKey("visualizer_style")
        val LANGUAGE = stringPreferencesKey("language")
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val SHOW_EXPLICIT = booleanPreferencesKey("show_explicit")
    }

    override val userPreferences: Flow<UserPreferences?> = context.dataStore.data
        .catch { e ->
            Timber.e(e, "Error reading preferences")
            emit(emptyPreferences())
        }
        .map { prefs ->
            UserPreferences(
                streamingQuality = AudioQuality.valueOf(prefs[Keys.STREAMING_QUALITY] ?: AudioQuality.HIGH.name),
                downloadQuality = AudioQuality.valueOf(prefs[Keys.DOWNLOAD_QUALITY] ?: AudioQuality.ULTRA.name),
                crossfadeDurationMs = prefs[Keys.CROSSFADE_DURATION] ?: 3000,
                normalizationEnabled = prefs[Keys.NORMALIZATION] ?: true,
                equalizerEnabled = prefs[Keys.EQUALIZER_ENABLED] ?: false,
                equalizerBands = runCatching {
                    val json = prefs[Keys.EQUALIZER_BANDS]
                    if (json != null) gson.fromJson(json, Array<EqualizerBand>::class.java).toList()
                    else EqualizerPreset.FLAT.bands
                }.getOrDefault(EqualizerPreset.FLAT.bands),
                equalizerPreset = EqualizerPreset.valueOf(prefs[Keys.EQUALIZER_PRESET] ?: EqualizerPreset.FLAT.name),
                bassBoostEnabled = prefs[Keys.BASS_BOOST_ENABLED] ?: false,
                bassBoostStrength = (prefs[Keys.BASS_BOOST_STRENGTH] ?: 0).toShort(),
                virtualizerEnabled = prefs[Keys.VIRTUALIZER_ENABLED] ?: false,
                virtualizerStrength = (prefs[Keys.VIRTUALIZER_STRENGTH] ?: 0).toShort(),
                themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name),
                dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
                downloadOnWifiOnly = prefs[Keys.DOWNLOAD_WIFI_ONLY] ?: true,
                autoDownload = prefs[Keys.AUTO_DOWNLOAD] ?: false,
                downloadPath = prefs[Keys.DOWNLOAD_PATH] ?: "",
                cacheSize = prefs[Keys.CACHE_SIZE] ?: (500 * 1024 * 1024L),
                lyricsEnabled = prefs[Keys.LYRICS_ENABLED] ?: true,
                floatingLyricsEnabled = prefs[Keys.FLOATING_LYRICS] ?: false,
                visualizerEnabled = prefs[Keys.VISUALIZER_ENABLED] ?: true,
                visualizerStyle = VisualizerStyle.valueOf(prefs[Keys.VISUALIZER_STYLE] ?: VisualizerStyle.BAR.name),
                language = prefs[Keys.LANGUAGE] ?: "en",
                skipSilence = prefs[Keys.SKIP_SILENCE] ?: false,
                showExplicit = prefs[Keys.SHOW_EXPLICIT] ?: true
            )
        }

    override suspend fun updateStreamingQuality(quality: AudioQuality) = update { it[Keys.STREAMING_QUALITY] = quality.name }
    override suspend fun updateThemeMode(mode: ThemeMode) = update { it[Keys.THEME_MODE] = mode.name }
    override suspend fun updateEqualizerEnabled(enabled: Boolean) = update { it[Keys.EQUALIZER_ENABLED] = enabled }
    override suspend fun updateEqualizerBands(bands: List<EqualizerBand>) = update { it[Keys.EQUALIZER_BANDS] = gson.toJson(bands) }
    override suspend fun updateCrossfadeDuration(ms: Int) = update { it[Keys.CROSSFADE_DURATION] = ms }
    override suspend fun updateDynamicColor(enabled: Boolean) = update { it[Keys.DYNAMIC_COLOR] = enabled }
    override suspend fun updateDownloadOnWifiOnly(wifiOnly: Boolean) = update { it[Keys.DOWNLOAD_WIFI_ONLY] = wifiOnly }
    override suspend fun updateLyricsEnabled(enabled: Boolean) = update { it[Keys.LYRICS_ENABLED] = enabled }
    override suspend fun updateVisualizerEnabled(enabled: Boolean) = update { it[Keys.VISUALIZER_ENABLED] = enabled }
    override suspend fun updateVisualizerStyle(style: VisualizerStyle) = update { it[Keys.VISUALIZER_STYLE] = style.name }
    override suspend fun updateSkipSilence(enabled: Boolean) = update { it[Keys.SKIP_SILENCE] = enabled }
    override suspend fun updateShowExplicit(show: Boolean) = update { it[Keys.SHOW_EXPLICIT] = show }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
