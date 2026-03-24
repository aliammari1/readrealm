# 📚 Library App - Android Frontend

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Android API](https://img.shields.io/badge/API-30+-brightgreen.svg)](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html)
[![Material Design 3](https://img.shields.io/badge/Material%20Design-3-673AB7.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![DeepSource](https://img.shields.io/badge/DeepSource-Active-brightgreen.svg)](.deepsource.toml)

> A modern, feature-rich Android library management application built with Kotlin and Jetpack Compose, featuring ML Kit integration, biometric authentication, and advanced book management capabilities.

## ✨ Features

### 📖 Core Library Management
- **Digital Book Catalog**: Browse and manage extensive book collections
- **Advanced Search**: Multi-criteria search with ML-powered suggestions
- **Book Scanner**: Camera-based barcode and text recognition using ML Kit
- **Personal Collections**: Organize books into custom categories and reading lists
- **Reading Progress**: Track reading status and progress analytics

### 🎨 Modern Android UI
- **Jetpack Compose**: Declarative UI with Material Design 3
- **Dynamic Theming**: Material You support with adaptive colors
- **Responsive Design**: Optimized for phones and tablets
- **Dark/Light Mode**: System-aware theme switching
- **Smooth Animations**: Fluid transitions and micro-interactions

### 🔐 Advanced Security
- **Biometric Authentication**: Fingerprint and face unlock support
- **Secure Storage**: Encrypted local data with DataStore
- **Social Login**: Google and Facebook authentication
- **Multi-factor Authentication**: Enhanced security options
- **Privacy Controls**: GDPR-compliant data handling

### 🧠 AI & Machine Learning
- **ML Kit Integration**: Text recognition, object detection, face detection
- **Smart Reply**: AI-powered chat suggestions
- **Language Detection**: Automatic language identification
- **Text Translation**: Real-time content translation
- **Image Labeling**: Automatic book cover categorization

### 📱 Rich Media Features
- **Camera Integration**: Capture and analyze book covers
- **Video Streaming**: ExoPlayer integration for multimedia content
- **PDF Generation**: Create and export reading reports
- **Real-time Chat**: Stream Chat integration for discussions
- **Push Notifications**: Smart notification system

### 🌐 Connectivity & Sync
- **REST API Integration**: Retrofit-powered backend communication
- **Real-time Updates**: Socket.IO for live data synchronization
- **Offline Support**: Room database for offline functionality
- **Cloud Sync**: Seamless data synchronization across devices
- **Network Optimization**: Intelligent caching and data management

## 🚀 Quick Start

### Prerequisites

- **Android Studio**: Koala Feature Drop (2024.1.2) or later
- **JDK**: 11 or higher
- **Android SDK**: API level 30+ (Android 11)
- **Kotlin**: 1.9.0 or later
- **Gradle**: 8.0 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/aliammari1/libraryapp-android-front.git
   cd libraryapp-android-front
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure dependencies**
   ```bash
   ./gradlew clean build
   ```

4. **Run the application**
   - Connect an Android device or start an emulator
   - Click "Run" (Shift+F10) or use the play button
   - Select your target device

### Environment Setup

#### API Configuration
Create `local.properties` file in the root directory:
```properties
# Backend API
API_BASE_URL="https://your-api-endpoint.com"
API_KEY="your-api-key"

# Google Services
GOOGLE_CLIENT_ID="your-google-client-id"

# Stream Chat
STREAM_API_KEY="your-stream-api-key"

# Facebook
FACEBOOK_APP_ID="your-facebook-app-id"
```

#### Google Services
1. Download `google-services.json` from Firebase Console
2. Place it in the `app/` directory
3. Configure Firebase Authentication and ML Kit

## 🏗️ Architecture

### Project Structure

```
app/
├── src/main/
│   ├── java/tn/esprit/libraryapp/
│   │   ├── data/                    # Data layer
│   │   │   ├── local/              # Room database, preferences
│   │   │   ├── remote/             # Retrofit API services
│   │   │   ├── repository/         # Repository implementations
│   │   │   └── models/             # Data models
│   │   ├── domain/                 # Business logic layer
│   │   │   ├── entities/           # Domain entities
│   │   │   ├── repository/         # Repository interfaces
│   │   │   └── usecases/           # Business use cases
│   │   ├── presentation/           # UI layer
│   │   │   ├── components/         # Reusable Compose components
│   │   │   ├── screens/            # Screen composables
│   │   │   ├── navigation/         # Navigation setup
│   │   │   ├── theme/              # App theming
│   │   │   └── viewmodel/          # ViewModels
│   │   ├── di/                     # Dependency injection
│   │   ├── utils/                  # Utility classes
│   │   └── MainActivity.kt         # Main activity
│   ├── res/                        # Resources
│   │   ├── drawable/               # Vector drawables
│   │   ├── mipmap/                 # App icons
│   │   ├── values/                 # Strings, colors, dimensions
│   │   └── xml/                    # XML configurations
│   └── AndroidManifest.xml         # App manifest
├── build.gradle.kts                # Module build configuration
└── proguard-rules.pro             # ProGuard rules
```

### Clean Architecture Layers

#### Data Layer
```kotlin
// Repository pattern implementation
@Repository
class BookRepositoryImpl @Inject constructor(
    private val localDataSource: BookLocalDataSource,
    private val remoteDataSource: BookRemoteDataSource
) : BookRepository {
    
    override suspend fun getBooks(): Flow<List<Book>> {
        return localDataSource.getBooks()
            .map { localBooks ->
                if (localBooks.isEmpty()) {
                    val remoteBooks = remoteDataSource.getBooks()
                    localDataSource.insertBooks(remoteBooks)
                    remoteBooks
                } else {
                    localBooks
                }
            }
    }
}
```

#### Domain Layer
```kotlin
// Use case example
class GetBooksUseCase @Inject constructor(
    private val repository: BookRepository
) {
    operator fun invoke(): Flow<Result<List<Book>>> = flow {
        try {
            repository.getBooks().collect { books ->
                emit(Result.success(books))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```

#### Presentation Layer
```kotlin
// Compose screen example
@Composable
fun BookListScreen(
    viewModel: BookListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn {
        items(uiState.books) { book ->
            BookItem(
                book = book,
                onClick = { viewModel.onBookClick(book) }
            )
        }
    }
}
```

## 🎨 Design System

### Material Design 3 Implementation

```kotlin
// Theme configuration
@Composable
fun LibraryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Component Design
- **Cards**: Material 3 elevated and filled cards
- **Buttons**: FAB, text, outlined, and filled variants
- **Navigation**: Bottom navigation with Material Motion
- **Dialogs**: Custom modal bottom sheets and dialogs
- **Lists**: Lazy lists with pull-to-refresh

## 🔧 Key Dependencies

### Core Android
```kotlin
dependencies {
    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    
    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Activity & Navigation
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel & Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
```

### Networking & Serialization
```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

### Local Storage
```kotlin
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")
}
```

### ML Kit & Camera
```kotlin
dependencies {
    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.mlkit:translate:17.0.1")
    
    // Camera
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-mlkit-vision:1.3.0")
}
```

## 🧪 Testing

### Test Structure

```
app/src/
├── test/                           # Unit tests
│   ├── java/tn/esprit/libraryapp/
│   │   ├── data/                  # Data layer tests
│   │   ├── domain/                # Domain layer tests
│   │   └── presentation/          # ViewModel tests
└── androidTest/                   # Instrumentation tests
    ├── java/tn/esprit/libraryapp/
    │   ├── ui/                    # Compose UI tests
    │   ├── database/              # Room database tests
    │   └── integration/           # Integration tests
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Specific test class
./gradlew test --tests "BookRepositoryTest"

# Test coverage
./gradlew jacocoTestReport
```

### Test Examples

#### Unit Test
```kotlin
@Test
fun `getBooks should return cached books when available`() = runTest {
    // Given
    val cachedBooks = listOf(
        Book(id = "1", title = "Test Book", author = "Test Author")
    )
    coEvery { localDataSource.getBooks() } returns flowOf(cachedBooks)
    
    // When
    val result = repository.getBooks().first()
    
    // Then
    assertEquals(cachedBooks, result)
    coVerify(exactly = 0) { remoteDataSource.getBooks() }
}
```

#### Compose UI Test
```kotlin
@Test
fun bookListDisplaysBooks() {
    composeTestRule.setContent {
        LibraryAppTheme {
            BookListScreen()
        }
    }
    
    composeTestRule
        .onNodeWithText("Test Book")
        .assertIsDisplayed()
}
```

## 🔒 Security Implementation

### Biometric Authentication
```kotlin
class BiometricAuthManager @Inject constructor(
    private val context: Context
) {
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            ContextCompat.getMainExecutor(context),
            authenticationCallback(onSuccess, onError)
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use your fingerprint or face")
            .setNegativeButtonText("Cancel")
            .build()
            
        biometricPrompt.authenticate(promptInfo)
    }
}
```

### Secure Storage
```kotlin
@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.createDataStore(
        name = "secure_preferences",
        serializer = EncryptedPreferencesSerializer
    )
    
    suspend fun storeAuthToken(token: String) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .putString("auth_token", token)
                .build()
        }
    }
}
```

## 📱 ML Kit Integration

### Text Recognition
```kotlin
class TextRecognitionManager @Inject constructor() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun recognizeText(imageProxy: ImageProxy): Result<String> {
        return suspendCoroutine { continuation ->
            val image = InputImage.fromMediaImage(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees
            )
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Result.success(visionText.text))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }
}
```

### Barcode Scanning
```kotlin
@Composable
fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit
) {
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }
    
    CameraPreview(
        onImageCaptured = { imageProxy ->
            barcodeScanner.process(
                InputImage.fromMediaImage(
                    imageProxy.image!!,
                    imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let(onBarcodeDetected)
            }
        }
    )
}
```

## 🚀 Building & Deployment

### Debug Build
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run with debugging
./gradlew installDebug && adb shell am start -n tn.esprit.libraryapp/.MainActivity
```

### Release Build
```bash
# Generate signed release APK
./gradlew assembleRelease

# Generate signed AAB for Play Store
./gradlew bundleRelease

# Upload to Play Console
# Use Play Console or fastlane
```

### Signing Configuration
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## 📊 Performance Optimization

### Memory Management
```kotlin
// Efficient image loading with Coil
@Composable
fun BookCoverImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

### LazyList Optimization
```kotlin
@Composable
fun OptimizedBookList(books: List<Book>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            items = books,
            key = { book -> book.id }  // Stable keys for recomposition
        ) { book ->
            BookItem(
                book = book,
                modifier = Modifier.animateItemPlacement()  // Smooth animations
            )
        }
    }
}
```

## 🌍 Internationalization

### String Resources
```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">Library App</string>
    <string name="books_title">My Books</string>
    <string name="search_hint">Search books...</string>
</resources>

<!-- res/values-ar/strings.xml -->
<resources>
    <string name="app_name">تطبيق المكتبة</string>
    <string name="books_title">كتبي</string>
    <string name="search_hint">البحث عن الكتب...</string>
</resources>
```

### RTL Support
```kotlin
@Composable
fun RTLAwareLayout() {
    CompositionLocalProvider(
        LocalLayoutDirection provides 
            if (Locale.getDefault().isRTL()) LayoutDirection.Rtl 
            else LayoutDirection.Ltr
    ) {
        // Your content here
    }
}
```

## 🤝 Contributing

### Development Guidelines

1. **Code Style**
   - Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
   - Use [ktlint](https://ktlint.github.io/) for code formatting
   - Write meaningful commit messages
   - Add documentation for public APIs

2. **Pull Request Process**
   ```bash
   # Create feature branch
   git checkout -b feature/amazing-feature
   
   # Make changes and test
   ./gradlew test
   ./gradlew ktlintCheck
   
   # Commit and push
   git commit -m "Add amazing feature"
   git push origin feature/amazing-feature
   
   # Create pull request
   ```

3. **Code Review Checklist**
   - [ ] Code follows style guidelines
   - [ ] Tests pass and coverage is maintained
   - [ ] Documentation is updated
   - [ ] Performance impact is considered
   - [ ] Accessibility guidelines are followed

## 📈 Analytics & Monitoring

### Firebase Analytics
```kotlin
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun logBookViewed(bookId: String, bookTitle: String) {
        val bundle = Bundle().apply {
            putString("book_id", bookId)
            putString("book_title", bookTitle)
        }
        firebaseAnalytics.logEvent("book_viewed", bundle)
    }
}
```

### Crashlytics Integration
```kotlin
// Report non-fatal exceptions
try {
    // Risky operation
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    // Handle gracefully
}
```

## 🗺️ Roadmap

### Version 2.0 Features
- [ ] Offline-first architecture with sync
- [ ] AR book scanning and placement
- [ ] Voice-controlled navigation
- [ ] Advanced ML recommendations
- [ ] Multi-user family accounts

### Version 2.1 Features
- [ ] Wear OS companion app
- [ ] Widget support for home screen
- [ ] Advanced sharing and social features
- [ ] Book clubs and community features
- [ ] Integration with external libraries

## 📚 Additional Resources

### Documentation
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Architecture Guide](https://developer.android.com/jetpack/guide)
- [Material Design 3](https://m3.material.io/)
- [ML Kit Documentation](https://developers.google.com/ml-kit)

### Learning Resources
- [Android Developers Codelab](https://developer.android.com/courses)
- [Compose Pathways](https://developer.android.com/courses/pathways/compose)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

## 👥 Author

### Ali Ammari
**Senior Android Developer & Mobile Architecture Specialist**

- 📧 **Email**: ali.ammari.dev@gmail.com
- 💼 **LinkedIn**: [Ali Ammari](https://linkedin.com/in/ali-ammari)
- 🐙 **GitHub**: [@aliammari1](https://github.com/aliammari1)
- 🌐 **Website**: [aacoder.me](https://aacoder.me)

### Expertise
- 🎯 **Specialization**: Native Android development with Kotlin and Jetpack Compose
- 🏆 **Experience**: 4+ years in mobile app development and architecture
- 🔧 **Skills**: Kotlin, Jetpack Compose, MVVM, Clean Architecture, ML Kit
- 📱 **Focus**: Performance optimization, user experience, modern Android development

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Ali Ammari

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

## 🙏 Acknowledgments

### Libraries & Frameworks
- **Jetpack Compose Team** - For the modern UI toolkit
- **Google ML Kit** - For powerful on-device ML capabilities
- **Stream** - For chat and video functionality
- **Square** - For Retrofit and OkHttp networking libraries

### Design & Inspiration
- **Material Design Team** - For comprehensive design system
- **Android Design Community** - For UI/UX inspiration and patterns
- **Open Source Community** - For excellent libraries and tools

### Learning & Development
- **Android Documentation** - Comprehensive guides and best practices
- **Kotlin Community** - Language development and resources
- **Stack Overflow** - Community support and problem solving
- **GitHub Developers** - Open source collaboration and learning

### Special Recognition
- **DeepSource** - For automated code quality analysis
- **Beta Testers** - For valuable feedback and bug reports
- **Android Community** - For knowledge sharing and collaboration
- **Mentors & Colleagues** - For guidance and professional development

## 📞 Support

### Getting Help
- **Issues**: [GitHub Issues](https://github.com/aliammari1/libraryapp-android-front/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aliammari1/libraryapp-android-front/discussions)
- **Email**: ali.ammari.dev@gmail.com
- **Documentation**: Check the wiki for detailed guides

### Community
- **Android Developers**: Join the official Android developer community
- **Kotlin Slack**: Connect with other Kotlin developers
- **Stack Overflow**: Tag questions with `android-kotlin-library-app`
- **Reddit**: Follow r/androiddev for updates and discussions

---

<div align="center">
  <p>Made with ❤️ and ☕ by <a href="https://github.com/aliammari1">Ali Ammari</a></p>
  <p>
    <a href="https://github.com/aliammari1/libraryapp-android-front">⭐ Star this repo</a> •
    <a href="https://github.com/aliammari1/libraryapp-android-front/issues">🐛 Report Bug</a> •
    <a href="https://github.com/aliammari1/libraryapp-android-front/issues">✨ Request Feature</a>
  </p>
</div>