# ReadLLM Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                          │
│                    (Jetpack Compose + Material 3)               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐              ┌──────────────────┐        │
│  │  MainActivity    │              │ ReaderActivity   │        │
│  │  (Library View)  │              │ (Book Reader)    │        │
│  │                  │              │                  │        │
│  │  - Book Grid     │              │  - Text Display  │        │
│  │  - Add Books     │              │  - Read-Aloud    │        │
│  │  - Progress      │              │  - TTS Controls  │        │
│  └────────┬─────────┘              └─────────┬────────┘        │
│           │                                   │                 │
└───────────┼───────────────────────────────────┼─────────────────┘
            │                                   │
            ▼                                   ▼
┌───────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                             │
│                   (Data Access Abstraction)                       │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ┌──────────────────────┐                       │
│                    │   BookRepository     │                       │
│                    │                      │                       │
│                    │  - getAllBooks()     │                       │
│                    │  - getBookById()     │                       │
│                    │  - updateProgress()  │                       │
│                    └──────────┬───────────┘                       │
│                               │                                   │
└───────────────────────────────┼───────────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                              │
│                      (Room + SQLite)                              │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐         ┌──────────────────────┐           │
│  │   AppDatabase    │────────▶│      BookDao         │           │
│  │                  │         │                      │           │
│  │  @Database       │         │  @Query INSERT       │           │
│  │  entities: [Book]│         │  @Update @Delete     │           │
│  └──────────────────┘         └──────────────────────┘           │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘


┌───────────────────────────────────────────────────────────────────┐
│                    CORE SERVICES LAYER                            │
│              (Business Logic & Processing)                        │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │            ReadAloudService (Orchestrator)              │     │
│  │                                                         │     │
│  │  - Coordinates TTS, OCR, and LLM                        │     │
│  │  - Manages reading state and progress                   │     │
│  │  - Handles play/pause/stop                              │     │
│  └────┬──────────────┬──────────────┬──────────────────────┘     │
│       │              │              │                            │
│       │              │              │                            │
│       ▼              ▼              ▼                            │
│  ┌─────────┐   ┌──────────┐   ┌──────────┐                      │
│  │   TTS   │   │   OCR    │   │   LLM    │                      │
│  │ Service │   │ Service  │   │ Service  │                      │
│  │         │   │          │   │          │                      │
│  │ Android │   │  ML Kit  │   │ TFLite   │                      │
│  │   API   │   │   Text   │   │ Vision-  │                      │
│  │         │   │  Recog.  │   │ Language │                      │
│  └─────────┘   └──────────┘   └──────────┘                      │
│                                                                   │
│                  ┌──────────────────────┐                        │
│                  │  EpubReaderService   │                        │
│                  │                      │                        │
│                  │  - Parse EPUB files  │                        │
│                  │  - Extract chapters  │                        │
│                  │  - Extract images    │                        │
│                  │  - Clean HTML        │                        │
│                  └──────────────────────┘                        │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## Read-Aloud Processing Flow

```
User Opens Book
       │
       ▼
┌──────────────────┐
│ Load EPUB File   │
│ (EpubReader)     │
└────────┬─────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Extract Content Segments            │
│                                     │
│  ┌────────────┐   ┌─────────────┐  │
│  │    Text    │   │   Images    │  │
│  │  Segments  │   │  Segments   │  │
│  └────────────┘   └─────────────┘  │
└───────────┬─────────────────────────┘
            │
            ▼
    User Taps Play
            │
            ▼
┌───────────────────────────────────────┐
│  ReadAloudService.readAloud()         │
│                                       │
│  For each segment:                    │
│    ├─ Is Text?                        │
│    │    └─▶ TextToSpeech.speak()     │
│    │                                  │
│    └─ Is Image?                       │
│         ├─▶ OCRService.extract()      │
│         │      │                      │
│         │      ├─ Detect equation?    │
│         │      ├─ Detect table?       │
│         │      └─ Extract text        │
│         │                             │
│         ├─▶ LLMService.explain()      │
│         │      │                      │
│         │      ├─ Rule-based (MVP)    │
│         │      └─ OR TFLite model     │
│         │                             │
│         └─▶ TextToSpeech.speak()      │
│                 (explanation)         │
└───────────────────────────────────────┘
            │
            ▼
    ┌─────────────┐
    │   Update    │
    │  Progress   │
    └─────────────┘
```

## Data Flow Diagram

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│  User   │────────▶│   UI     │────────▶│Repository│
│ Action  │         │ Layer    │         │  Layer   │
└─────────┘         └──────────┘         └────┬─────┘
                                               │
                                               ▼
                                          ┌─────────┐
                                          │Database │
                                          │ (Room)  │
                                          └─────────┘

┌──────────┐        ┌──────────┐         ┌──────────┐
│  EPUB    │───────▶│  Reader  │────────▶│  Text +  │
│  File    │        │ Service  │         │  Images  │
└──────────┘        └──────────┘         └────┬─────┘
                                              │
                                              ▼
                                         ┌─────────┐
                                         │   OCR   │
                                         │ Service │
                                         └────┬────┘
                                              │
                                              ▼
                                         ┌─────────┐
                                         │   LLM   │
                                         │ Service │
                                         └────┬────┘
                                              │
                                              ▼
                                         ┌─────────┐
                                         │   TTS   │
                                         │ Service │
                                         └─────────┘
```

## Component Dependencies

```
MainActivity
    ├── BookRepository
    │   └── AppDatabase (Room)
    └── Compose UI Theme

ReaderActivity
    ├── EpubReaderService
    │   └── epublib library
    ├── ReadAloudService
    │   ├── OCRService
    │   │   └── ML Kit Text Recognition
    │   ├── LLMService
    │   │   └── TensorFlow Lite
    │   └── Android TextToSpeech API
    └── Compose UI Theme
```

## Technology Stack Layers

```
┌────────────────────────────────────────────┐
│         PRESENTATION LAYER                 │
│  Jetpack Compose + Material Design 3       │
│  (MainActivity, ReaderActivity, Theme)     │
└────────────────────────────────────────────┘
                    ▲
                    │
┌────────────────────────────────────────────┐
│         DOMAIN LAYER                       │
│  Business Logic & Use Cases                │
│  (Services: Reader, OCR, LLM, TTS)         │
└────────────────────────────────────────────┘
                    ▲
                    │
┌────────────────────────────────────────────┐
│         DATA LAYER                         │
│  Repository Pattern + Room Database        │
│  (BookRepository, AppDatabase, Book)       │
└────────────────────────────────────────────┘
                    ▲
                    │
┌────────────────────────────────────────────┐
│      EXTERNAL DEPENDENCIES                 │
│  • epublib (EPUB parsing)                  │
│  • ML Kit (OCR)                            │
│  • TensorFlow Lite (AI models)             │
│  • Android TTS (Text-to-Speech)            │
│  • Room (Database ORM)                     │
└────────────────────────────────────────────┘
```

## State Management Flow

```
User Action
    │
    ▼
┌────────────────┐
│  UI State      │
│  (Compose)     │
└────────┬───────┘
         │
         ▼
┌────────────────┐
│  ViewModel     │  (Future: Add ViewModels for MVVM)
│  (Optional)    │
└────────┬───────┘
         │
         ▼
┌────────────────┐
│  Repository    │
└────────┬───────┘
         │
         ├──▶ Database (Persistent State)
         │
         └──▶ Services (Processing State)
```

## Key Design Patterns

1. **MVVM Architecture**: Separation of UI, business logic, and data
2. **Repository Pattern**: Abstract data access
3. **Service Layer**: Encapsulate complex operations
4. **Dependency Injection**: Services injected into activities
5. **Observer Pattern**: Room Flow for reactive data
6. **Strategy Pattern**: Fallback from model to rule-based explanations

## Performance Considerations

- **OCR**: Runs on background thread (Coroutines)
- **LLM**: GPU delegation available via TFLite
- **TTS**: Native Android API (optimized)
- **Database**: Room with Flow for reactive updates
- **EPUB Parsing**: Cached chapter content

## Security & Privacy

```
┌────────────────────────────────────┐
│        User's Device               │
│                                    │
│  ┌──────────────────────────────┐ │
│  │   All Processing Local       │ │
│  │   - EPUB parsing             │ │
│  │   - OCR extraction           │ │
│  │   - LLM inference            │ │
│  │   - TTS generation           │ │
│  └──────────────────────────────┘ │
│                                    │
│  No external network calls         │
│  No cloud processing              │
│  No data sharing                  │
└────────────────────────────────────┘
```
