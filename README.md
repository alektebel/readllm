# ReadLLM - Accessible eBook Reader with AI-Powered Read-Aloud

ReadLLM is an Android eBook reader app similar to ReadEra, with advanced accessibility features powered by AI. It reads books aloud while intelligently explaining images, equations, tables, and other visual content that would be inaccessible to visually impaired readers or in audio-only mode.

## Features (MVP)

### Core Reading Features
- **EPUB Support**: Read EPUB format ebooks with a clean, customizable interface
- **Book Library**: Manage your ebook collection with reading history and progress tracking
- **Reading Progress**: Automatically saves your position in each book

### AI-Powered Read-Aloud Mode
- **Text-to-Speech**: High-quality voice reading of book content
- **OCR Integration**: Extracts text from images using Google ML Kit
- **Visual Content Explanation**: AI-powered descriptions of:
  - Images and illustrations
  - Mathematical equations
  - Tables and charts
  - Diagrams and technical content
- **Adjustable Reading Speed**: Control playback speed (0.5x - 2.0x)
- **Seamless Integration**: Automatically detects and explains visual elements while reading

### Accessibility
- Designed for visually impaired users
- Comprehensive audio descriptions
- Easy-to-use controls

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **EPUB Parser**: epublib-core
- **OCR**: Google ML Kit Text Recognition
- **AI Model**: TensorFlow Lite (ready for lightweight vision-language models)
- **TTS**: Android TextToSpeech API
- **Database**: Room (SQLite)
- **Architecture**: MVVM with Repository pattern

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26 or higher
- JDK 17

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd readllm
```

2. Open the project in Android Studio

3. Sync Gradle dependencies:
   - Android Studio should automatically prompt to sync
   - Or manually: `File -> Sync Project with Gradle Files`

4. Build and run:
   - Connect an Android device or start an emulator
   - Click Run (▶️) or press Shift+F10

### Adding a Lightweight LLM Model (Optional)

For production-quality visual explanations, you need to add a vision-language model:

1. **Recommended Models** (quantized for mobile):
   - **MobileVLM** (1.4B params, 4-bit quantized) - Best balance of size/quality
   - **TinyLLaVA** (1.5B params) - Good for image understanding
   - **Phi-2 + Vision Adapter** (2.7B params, int8) - Higher quality, larger size

2. **Convert to TensorFlow Lite**:
```bash
# Example for converting a model (requires Python environment)
python convert_to_tflite.py --model mobilevlm-1.4b --quantize int4
```

3. **Add to project**:
   - Create directory: `app/src/main/assets/models/`
   - Place the `.tflite` file there
   - Update `LLMService.kt` to load your specific model

4. **Model loading code** (already structured in `LLMService.kt`):
```kotlin
// Uncomment and modify in LLMService.kt init block:
interpreter = Interpreter(loadModelFile("models/your_model.tflite"))
```

## Project Structure

```
app/src/main/java/com/readllm/app/
├── MainActivity.kt              # Library screen
├── ReaderActivity.kt            # Book reading screen
├── model/
│   └── Book.kt                  # Book data model
├── reader/
│   └── EpubReaderService.kt     # EPUB parsing and content extraction
├── ocr/
│   └── OCRService.kt            # Image text extraction
├── llm/
│   └── LLMService.kt            # AI visual content explanation
├── tts/
│   └── ReadAloudService.kt      # Text-to-speech orchestration
├── database/
│   └── AppDatabase.kt           # Room database
├── repository/
│   └── BookRepository.kt        # Data repository layer
└── ui/
    └── theme/
        └── Theme.kt             # Material 3 theme
```

## Usage

### Adding Books
1. Tap the + button on the library screen
2. Select an EPUB file from your device
3. The book will be imported and appear in your library

### Reading Books
1. Tap a book in your library to open it
2. Swipe or tap to navigate between pages
3. Your reading position is automatically saved

### Read-Aloud Mode
1. Open a book
2. Tap the volume icon in the toolbar
3. Press the play button to start reading
4. The app will:
   - Read text content aloud
   - Detect images, equations, and tables
   - Provide AI-generated explanations for visual content
5. Adjust reading speed with the slider
6. Pause/resume or stop as needed

## TODO - Future Features

### High Priority

- [ ] **PDF Support with EPUB Conversion**
  - Import PDF files
  - Convert PDF to EPUB format for better reading experience
  - Use Apache PDFBox or similar library
  - Maintain formatting and images during conversion
  - Background conversion with progress indicator

- [ ] **Reading History & Statistics**
  - Track reading time per book
  - Daily/weekly reading goals
  - Reading streak tracking
  - Books finished counter
  - Favorite genres/authors
  - Reading heatmap calendar view

- [ ] **Advanced LLM Integration**
  - Replace rule-based explanations with actual vision-language model
  - Support for equation understanding (LaTeX generation)
  - Context-aware explanations (based on book content)
  - Multilingual support

- [ ] **Interactive Reading Comprehension & Quiz System**
  - AI-generated comprehension questions at natural pause points
  - Detect chapter endings, section breaks, and natural pauses
  - Ask 2-5 questions about what was just read/heard
  - Questions types:
    - Factual recall (who, what, when, where)
    - Conceptual understanding (why, how)
    - Analysis and inference
    - Visual content comprehension (from images/equations explained)
  - Score tracking per chapter
  - Overall book comprehension score
  - Difficulty adjustment based on user performance
  - Optional mode (can be enabled/disabled)
  - Review incorrect answers with explanations
  - Progress dashboard showing:
    - Chapter-by-chapter comprehension scores
    - Weak areas identification
    - Time spent on each chapter vs comprehension
    - Improvement trends over time
  - Export comprehension reports
  - Use LLM to generate contextual questions based on actual content

### Medium Priority

- [ ] **Additional Format Support**
  - MOBI format
  - AZW/AZW3 (Kindle formats)
  - PDF direct reading (without conversion)
  - Plain text files
  - HTML documents

- [ ] **Enhanced Reading Features**
  - Bookmarks
  - Highlights and annotations
  - Text search within books
  - Table of contents navigation
  - Font size and style customization
  - Reading themes (day, night, sepia)
  - Adjustable margins and line spacing

- [ ] **Cloud Sync**
  - Sync reading progress across devices
  - Cloud backup of library and annotations
  - Support for Google Drive, Dropbox

- [ ] **Advanced TTS Features**
  - Multiple voice options
  - Voice customization (pitch, tone)
  - Background playback
  - Sleep timer
  - Headphone controls support

### Low Priority

- [ ] **Social Features**
  - Reading challenges with friends
  - Book recommendations
  - Reading groups/clubs
  - Share quotes and highlights

- [ ] **Accessibility Enhancements**
  - High contrast themes
  - Dyslexia-friendly fonts
  - Gesture navigation
  - Voice commands

- [ ] **Library Management**
  - Collections/folders
  - Tags and labels
  - Advanced search and filters
  - Import/export library metadata
  - Integration with Calibre

- [ ] **Performance Optimizations**
  - Model quantization improvements
  - Faster EPUB parsing
  - Caching for images and content
  - Battery optimization

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Guidelines

1. Follow Kotlin coding conventions
2. Use Jetpack Compose for UI components
3. Maintain MVVM architecture
4. Write descriptive commit messages
5. Test on multiple Android versions (API 26+)

## License

[Add your license here]

## Acknowledgments

- **epublib** for EPUB parsing
- **Google ML Kit** for OCR capabilities
- **TensorFlow Lite** for on-device AI inference
- **ReadEra** for inspiration on UI/UX design

## Privacy

This app processes all content locally on your device. No book content, reading history, or personal data is sent to external servers. The AI models run entirely on-device.

## System Requirements

- **Minimum**: Android 8.0 (API 26)
- **Recommended**: Android 12.0 (API 31) or higher
- **Storage**: 100MB+ for app and models
- **RAM**: 2GB+ (4GB+ recommended for better LLM performance)

## Known Limitations (MVP)

- Rule-based visual content explanations (until LLM model is integrated)
- EPUB format only (PDF conversion coming soon)
- Basic reading statistics (detailed history coming soon)
- Single device only (cloud sync coming soon)

## Support

For issues, questions, or feature requests, please open an issue on GitHub.
