# Getting Started with ReadLLM Development

## Quick Start (5 minutes)

### 1. Environment Setup

Ensure you have:
- Android Studio Hedgehog or later
- JDK 17 installed
- Android SDK 34 installed
- At least 4GB RAM available

### 2. Clone and Build

```bash
# Clone the repository
git clone <your-repo-url>
cd readllm

# Open in Android Studio
# File -> Open -> Select the readllm folder

# Wait for Gradle sync to complete
# This may take 2-5 minutes on first run
```

### 3. Run the App

```bash
# Option A: Using Android Studio
1. Click the green Run button (▶️)
2. Select a device or emulator
3. Wait for build and installation

# Option B: Using command line
./gradlew installDebug
```

### 4. Test Basic Functionality

The MVP includes:
1. **Library Screen**: Empty by default (file picker not yet implemented)
2. **Reader Screen**: Shows sample content
3. **Read-Aloud Mode**: Click volume icon to enable
4. **TTS Controls**: Play/pause, speed adjustment

## Development Workflow

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Building Release APK

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Project Architecture

### MVVM Pattern

```
UI (Compose) -> ViewModel -> Repository -> Data Source
                                        -> Room Database
                                        -> Services (EPUB, OCR, LLM, TTS)
```

### Key Components

1. **EpubReaderService**: Parses EPUB files, extracts text and images
2. **OCRService**: Recognizes text in images using ML Kit
3. **LLMService**: Generates explanations for visual content
4. **ReadAloudService**: Orchestrates TTS with visual explanations
5. **BookRepository**: Manages book data persistence

## Next Steps for Development

### Immediate Priorities

1. **Implement File Picker** (MainActivity.kt:53)
   - Add storage permissions handling
   - Integrate Android Storage Access Framework
   - Copy selected EPUB to app storage

2. **Connect EPUB Reader** (ReaderActivity.kt)
   - Load actual EPUB content
   - Parse chapters and pages
   - Render formatted text

3. **Integrate Real LLM Model**
   - Download and convert a vision-language model
   - Update LLMService to use actual model
   - Test inference performance

### Feature Roadmap

See [README.md](README.md) TODO section for complete roadmap.

## Common Issues and Solutions

### Issue: Gradle sync fails
**Solution**: Check internet connection, clear Gradle cache:
```bash
./gradlew clean
rm -rf .gradle
```

### Issue: Build fails with "Duplicate class" error
**Solution**: Check for dependency conflicts in build.gradle.kts

### Issue: App crashes on launch
**Solution**: Check logcat for stack trace:
```bash
adb logcat | grep ReadLLM
```

### Issue: TTS not working
**Solution**: Ensure device has TTS engine installed (Google Text-to-Speech recommended)

## Debugging Tips

### Enable Debug Logging

Add to Application class:
```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### Profiling Performance

Use Android Profiler:
1. Run app in debug mode
2. View -> Tool Windows -> Profiler
3. Monitor CPU, Memory, Network

### Testing on Different Devices

Priority device types:
- Low-end: Android 8.0, 2GB RAM
- Mid-range: Android 11, 4GB RAM  
- High-end: Android 13+, 8GB RAM

## Code Style

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

Key points:
- Use 4 spaces for indentation
- Max line length: 120 characters
- Use meaningful variable names
- Add KDoc comments for public APIs

## Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and test thoroughly
3. Commit with descriptive messages
4. Push and create pull request

## Resources

- [Android Developers Guide](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [TensorFlow Lite Documentation](https://www.tensorflow.org/lite)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

## Getting Help

- Check existing issues on GitHub
- Read documentation in `/docs` folder
- Ask questions in discussions

## License

[Add license information]
