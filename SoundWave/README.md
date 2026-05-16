# 🎵 SoundWave - Premium Android Music Streaming App

A production-ready, high-performance Android music streaming application built with Jetpack Compose, Material 3, and ExoPlayer/Media3. Features ultra-fast playback, intelligent buffering, offline downloads, synced lyrics, equalizer, and a premium Spotify-inspired UI.

## ✨ Features

### 🎶 Core Music Features
- **Ultra-fast streaming** with intelligent buffering and adaptive bitrate
- **Multi-source fallback** (JioSaavn, Audius, proxy server)
- **Offline downloads** with configurable quality
- **Background playback** with media session controls
- **Gapless playback** with crossfade support
- **Queue management** (add, remove, reorder tracks)
- **Smart shuffle** and repeat modes

### 🎨 UI/UX
- **Material 3 Design** with dynamic theming (Material You)
- **AMOLED dark mode** for battery optimization
- **Glassmorphism effects** on mini-player and cards
- **Smooth animations** at 60fps (no frame drops)
- **Shimmer loading placeholders**
- **Gesture-based navigation**
- **Responsive layouts** for phones and tablets

### 🎵 Audio Features
- **5-band equalizer** with presets (Flat, Bass Boost, Treble Boost, Vocal, Electronic, Rock, Jazz)
- **Bass boost** and virtualizer effects
- **Audio normalization**
- **Skip silence** detection
- **Playback speed control** (0.5x - 2x)

### 📝 Lyrics & Metadata
- **Synced lyrics** with real-time highlighting
- **Full-screen lyrics view** with auto-scroll
- **Metadata extraction** from multiple sources
- **Album art** with palette-based theming

### 📚 Library Management
- **Create and manage playlists**
- **Favorites** (tracks, albums, playlists, artists)
- **Recently played** history
- **Download management**
- **Search history**

### 🔍 Discovery
- **Home feed** with trending, new releases, featured playlists
- **Search** across tracks, albums, artists, playlists
- **Mood-based playlists**
- **Recommendations** based on listening history

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt/Dagger
- **Database**: Room with Flow
- **Networking**: Retrofit + OkHttp
- **Media**: Media3 (ExoPlayer) with caching
- **Image Loading**: Coil
- **Async**: Coroutines + Flow
- **Paging**: Paging 3
- **Background Tasks**: WorkManager
- **Preferences**: DataStore

### Project Structure
```
app/src/main/java/com/soundwave/app/
├── core/
│   ├── audio/              # ExoPlayer, playback service, stream resolver
│   ├── cache/              # Audio caching with Media3
│   ├── di/                 # Hilt modules
│   ├── network/            # API clients
│   └── utils/              # Utilities
├── data/
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # API interfaces, DTOs
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic
└── presentation/
    ├── ui/                 # Compose screens
    ├── viewmodel/          # ViewModels
    ├── navigation/         # Navigation graph
    └── theme/              # Material 3 theme
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Minimum SDK 26 (Android 8.0)

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/soundwave-android.git
cd soundwave-android
```

2. **Configure API Keys** (Optional)
Create `local.properties` in the root directory:
```properties
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
LASTFM_API_KEY=your_lastfm_api_key
```

3. **Build the project**
```bash
./gradlew assembleDebug
```

4. **Run the app**
- Connect an Android device or start an emulator
- Click "Run" in Android Studio or:
```bash
./gradlew installDebug
```

## 🔧 Configuration

### Streaming Quality
Default streaming quality can be configured in `UserPreferencesRepository`:
- **LOW**: 96 kbps
- **MEDIUM**: 128 kbps
- **HIGH**: 320 kbps (default)
- **ULTRA**: Lossless (when available)

### Proxy Server
The app includes an embedded Ktor proxy server for metadata fetching and stream URL resolution. Configure in `BuildConfig`:
```kotlin
PROXY_BASE_URL = "http://10.0.2.2:8080"  // Android emulator
```

For production, deploy the proxy server separately and update the URL.

## 📦 Building for Release

### Generate Signed APK

1. **Create keystore**:
```bash
keytool -genkey -v -keystore soundwave.keystore -alias soundwave -keyalg RSA -keysize 2048 -validity 10000
```

2. **Create `keystore.properties`**:
```properties
storeFile=soundwave.keystore
storePassword=your_store_password
keyAlias=soundwave
keyPassword=your_key_password
```

3. **Build release APK**:
```bash
./gradlew assembleRelease
```

APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

### Build App Bundle (for Play Store)
```bash
./gradlew bundleRelease
```

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Instrumented Tests
```bash
./gradlew connectedDebugAndroidTest
```

## 🎨 Customization

### Theme Colors
Edit `app/src/main/java/com/soundwave/app/presentation/ui/theme/Theme.kt`:
```kotlin
val SoundWaveGreen = Color(0xFF1ED760)  // Primary color
val SoundWaveBlack = Color(0xFF000000)  // Background (AMOLED)
```

### API Endpoints
Update base URLs in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "JIOSAAVN_BASE_URL", "\"https://www.jiosaavn.com/api.php\"")
buildConfigField("String", "LYRICS_API_URL", "\"https://lrclib.net\"")
```

## 📱 Screenshots

(Add screenshots here)

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [JioSaavn API](https://www.jiosaavn.com/) for music metadata
- [lrclib.net](https://lrclib.net/) for synced lyrics
- [Material 3](https://m3.material.io/) design system
- [Media3/ExoPlayer](https://developer.android.com/media/media3) for audio playback
- Inspired by Spotify's UI/UX design

## 📞 Support

For issues, questions, or contributions:
- Open an [issue](https://github.com/yourusername/soundwave-android/issues)
- Email: support@soundwave.app

## 🔮 Roadmap

- [ ] Android Auto support
- [ ] Wear OS app
- [ ] Social features (share playlists, follow friends)
- [ ] AI-powered recommendations
- [ ] Podcast support
- [ ] Sleep timer
- [ ] Gapless crossfade improvements
- [ ] More streaming sources

---

**Made with ❤️ by SoundWave Team**
