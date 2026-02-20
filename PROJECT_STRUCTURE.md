# ReadLLM Project Structure

```
readllm/
├── README.md                           # Main documentation with features and TODO
├── GETTING_STARTED.md                  # Development setup guide
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Project settings
├── gradle.properties                   # Gradle properties
├── .gitignore                          # Git ignore rules
│
└── app/
    ├── build.gradle.kts                # App module build config
    ├── proguard-rules.pro              # ProGuard rules for release builds
    │
    └── src/
        └── main/
            ├── AndroidManifest.xml     # App manifest with permissions
            │
            ├── assets/
            │   └── models/
            │       └── README.md       # Model integration guide
            │
            ├── res/
            │   ├── values/
            │   │   ├── strings.xml     # String resources
            │   │   └── themes.xml      # Material theme
            │   └── xml/
            │       ├── backup_rules.xml
            │       └── data_extraction_rules.xml
            │
            └── java/com/readllm/app/
                ├── MainActivity.kt              # Library screen
                ├── ReaderActivity.kt            # Book reading UI
                │
                ├── model/
                │   └── Book.kt                  # Book entity (Room)
                │
                ├── database/
                │   └── AppDatabase.kt           # Room database + DAO
                │
                ├── repository/
                │   └── BookRepository.kt        # Data layer
                │
                ├── reader/
                │   └── EpubReaderService.kt     # EPUB parsing
                │
                ├── ocr/
                │   └── OCRService.kt            # ML Kit text recognition
                │
                ├── llm/
                │   └── LLMService.kt            # TFLite vision-language model
                │
                ├── tts/
                │   └── ReadAloudService.kt      # TTS orchestration
                │
                └── ui/
                    └── theme/
                        └── Theme.kt             # Compose theme
```

## File Descriptions

### Root Level

- **README.md**: Complete project documentation including features, setup, and comprehensive TODO list
- **GETTING_STARTED.md**: Developer onboarding guide with common issues and solutions
- **build.gradle.kts**: Root Gradle configuration with plugin versions
- **settings.gradle.kts**: Project settings and module includes
- **gradle.properties**: Gradle JVM arguments and Android configuration

### App Module

#### Configuration
- **build.gradle.kts**: Dependencies (Compose, ML Kit, TFLite, Room, epublib)
- **proguard-rules.pro**: Keep rules for TFLite, ML Kit, Room, and epublib
- **AndroidManifest.xml**: Permissions (storage, internet) and activity declarations

#### Core Application Code

**UI Layer** (Jetpack Compose)
- `MainActivity.kt`: Book library with grid/list view
- `ReaderActivity.kt`: EPUB reader with read-aloud controls
- `ui/theme/Theme.kt`: Material 3 theming (light/dark modes)

**Domain Layer** (Business Logic)
- `reader/EpubReaderService.kt`: Parse EPUB, extract text/images, clean HTML
- `ocr/OCRService.kt`: Google ML Kit integration, equation/table detection
- `llm/LLMService.kt`: TFLite model wrapper, visual content explanation
- `tts/ReadAloudService.kt`: TextToSpeech API, content orchestration

**Data Layer**
- `model/Book.kt`: Room entity for books
- `database/AppDatabase.kt`: Room database with BookDao
- `repository/BookRepository.kt`: Abstract data access

#### Resources
- `res/values/strings.xml`: All UI strings
- `res/values/themes.xml`: Material theme configuration
- `assets/models/`: TFLite model storage (gitignored)

## Code Statistics (MVP)

- **Total Files**: 22
- **Kotlin Files**: 11
- **Lines of Code**: ~1,500 (excluding comments)
- **Key Features**: 
  - EPUB reading ✓
  - OCR extraction ✓
  - AI explanations (rule-based MVP) ✓
  - Text-to-speech ✓
  - Book library ✓

## Technology Breakdown

| Component | Technology | Purpose |
|-----------|-----------|---------|
| UI | Jetpack Compose | Modern declarative UI |
| EPUB Parser | epublib-core | Read EPUB files |
| OCR | Google ML Kit | Extract text from images |
| AI Model | TensorFlow Lite | Explain visual content |
| TTS | Android TextToSpeech | Read text aloud |
| Database | Room (SQLite) | Store book metadata |
| Architecture | MVVM | Separation of concerns |
| Language | Kotlin | Modern Android development |

## Build Variants

Currently configured:
- **Debug**: Full logging, no optimization
- **Release**: ProGuard enabled, optimized

## Dependencies Summary

**Core Android**
- AndroidX Core, AppCompat, Material
- Jetpack Compose (UI, Material3, Tooling)

**Features**
- epublib-core 3.1 (EPUB parsing)
- ML Kit Text Recognition 16.0.0 (OCR)
- TensorFlow Lite 2.14.0 (AI models)
- Room 2.6.1 (Database)

**Utilities**
- Kotlin Coroutines 1.7.3
- Commons IO 2.15.1
- Coil 2.5.0 (Image loading)

## Next Development Tasks

1. Implement file picker in MainActivity
2. Connect EpubReaderService to ReaderActivity
3. Add actual TFLite vision-language model
4. Implement PDF to EPUB conversion
5. Add reading history and statistics

See [README.md TODO section](README.md#todo---future-features) for complete roadmap.
