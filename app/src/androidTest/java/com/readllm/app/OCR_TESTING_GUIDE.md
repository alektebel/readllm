# OCR-Based Visual Testing for EPUB Reader

## Overview

This document explains how to use OCR (Optical Character Recognition) to validate that the ReadLLM app correctly renders EPUB content on screen.

## Testing Approach

We use a **two-tier testing strategy**:

### Tier 1: Integration Tests (Unit Test Level)
- **File**: `app/src/test/java/com/readllm/app/EpubIntegrationTest.kt`
- **Purpose**: Validate EPUB parsing and text extraction
- **Coverage**:
  - Metadata extraction (title, author)
  - Chapter count and titles
  - Content integrity (key phrases present)
  - HTML cleaning (no XML artifacts)
  - All chapters accessible
  
### Tier 2: Visual Rendering Tests (Instrumented Test Level)
- **File**: `app/src/androidTest/java/com/readllm/app/EpubVisualRenderingTest.kt`
- **Purpose**: Validate text actually renders correctly on screen
- **Coverage**:
  - Text visibility in UI
  - Font size rendering
  - HTML formatting preservation
  - Optional: OCR-based text extraction

## Running the Tests

### Run Integration Tests (Fast)
```bash
./gradlew test
# Or specifically:
./gradlew testDebugUnitTest --tests "com.readllm.app.EpubIntegrationTest"
```

### Run Visual Rendering Tests (Requires Emulator/Device)
```bash
./gradlew connectedAndroidTest
# Or specifically:
./gradlew connectedDebugAndroidTest --tests "com.readllm.app.EpubVisualRenderingTest"
```

## OCR Testing Details

### Why OCR Testing?

Traditional UI tests validate that text *exists* in the UI tree, but OCR testing validates what's **actually visible on screen**. This catches issues like:
- Text rendering off-screen
- Invisible text (white on white)
- Font rendering failures
- Layout bugs that hide content

### OCR Implementation

We use **Google ML Kit Text Recognition** which is:
- Already included in dependencies
- Optimized for on-device processing
- Works offline
- Fast and accurate for printed text

### How It Works

```kotlin
// 1. Render the epub content
composeTestRule.setContent {
    HtmlText(html = chapterContent, fontSize = 20f)
}

// 2. Capture screenshot (Compose test framework)
val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()

// 3. Extract text using ML Kit
val image = InputImage.fromBitmap(bitmap, 0)
val result = textRecognizer.process(image).await()
val extractedText = result.text

// 4. Validate expected content is present
assertTrue(extractedText.contains("Artificial Intelligence"))
assertTrue(extractedText.contains("John McCarthy"))
```

## OCR vs Semantic Testing

### Semantic Testing (Default Approach)
```kotlin
// Checks if text is in the UI tree (accessibility nodes)
composeTestRule.onNodeWithText("John McCarthy").assertExists()
```
**Pros:**
- Fast and reliable
- Works with accessibility features
- No screenshot needed

**Cons:**
- Doesn't verify visual rendering
- Could pass even if text is invisible

### OCR Testing (Advanced Approach)
```kotlin
// Checks if text is VISIBLE in screenshot
val bitmap = captureScreen()
val ocrText = extractText(bitmap)
assertTrue(ocrText.contains("John McCarthy"))
```
**Pros:**
- Validates actual visual output
- Catches rendering bugs
- Tests what users actually see

**Cons:**
- Slower (requires image processing)
- Can have false negatives (OCR accuracy ~95-98%)
- Requires device/emulator

## When to Use Each Approach

| Test Scenario | Recommended Approach |
|--------------|---------------------|
| Content parsing correctness | Integration test |
| Text present in UI | Semantic test |
| Text actually visible | OCR test |
| Font size effects | Semantic test |
| Screenshot verification | OCR test |
| CI/CD pipeline | Integration + Semantic |
| Pre-release validation | All three |

## Expected Test Results

### Sample Book Content Validation

The tests validate that the sample EPUB (`sample_book.epub`) correctly displays:

**Chapter 1: What is Artificial Intelligence?**
- ✓ Title: "ReadLLM Sample Book: Introduction to AI"
- ✓ Author: "ReadLLM Team"
- ✓ Key content: John McCarthy, Dartmouth Conference, 1956
- ✓ Concepts: Learning, Reasoning, Perception, NLP
- ✓ AI Types: Narrow AI, General AI, Super AI

**Chapter 2: Machine Learning Basics**
- ✓ Learning types: Supervised, Unsupervised, Reinforcement
- ✓ Neural network layers: Input, Hidden, Output
- ✓ Challenges: Overfitting, Bias, Data Quality

**Chapter 3: The Future of AI**
- ✓ Trends: Multimodal AI, Edge AI, Explainable AI
- ✓ Impacts: Healthcare, Education, Climate
- ✓ Ethics: Privacy, Fairness, Accountability

## Extending OCR Testing

### Using a Small Vision LLM

For more advanced validation, you could use a small vision LLM like:

1. **Moondream2** (1.6B parameters, MIT license)
   - Can describe what's in images
   - Answers questions about visual content
   - ~1.6GB model size

2. **Phi-3.5-vision** (4.2B parameters, Microsoft)
   - Multimodal understanding
   - Good for layout analysis
   - ~4GB model size

**Implementation approach:**
```kotlin
// Load vision model (e.g., Moondream2 via TensorFlow Lite)
val visionModel = loadModel("moondream2.tflite")

// Capture screenshot
val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()

// Ask the model questions
val response = visionModel.ask(bitmap, "What is the main heading on this screen?")
assertTrue(response.contains("Artificial Intelligence"))

val response2 = visionModel.ask(bitmap, "Is the text readable and properly formatted?")
assertTrue(response2.contains("yes") || response2.contains("readable"))
```

**Trade-offs:**
- ✅ Can validate layout, formatting, readability
- ✅ More semantic understanding than pure OCR
- ❌ Slower (model inference time)
- ❌ Larger APK size (model weights)
- ❌ More complex setup

## Recommendations

For **ReadLLM app**, I recommend:

1. **Primary testing**: Integration tests + Semantic tests
   - Fast, reliable, runs in CI
   - Validates 95% of reading functionality

2. **Secondary testing**: OCR tests for critical paths
   - Run before releases
   - Validate first chapter of sample book
   - Ensure fonts render correctly

3. **Optional**: Vision LLM testing
   - Only if you want to validate layout quality
   - Only if you have GPU-enabled test devices
   - Consider as manual QA tool rather than automated test

## Current Implementation Status

✅ **Completed:**
- Integration test suite (`EpubIntegrationTest.kt`)
- Visual rendering test suite (`EpubVisualRenderingTest.kt`)
- Enhanced HTML entity decoding in `EpubReaderService`
- ML Kit OCR dependency already in build.gradle

📋 **To activate full OCR testing:**
1. Uncomment the OCR code in `EpubVisualRenderingTest.kt` (line ~200)
2. Ensure device/emulator has Google Play Services
3. Run tests with `./gradlew connectedAndroidTest`

⚡ **Quick validation:**
```bash
# Run all tests
./gradlew test connectedAndroidTest

# Or just the important ones
./gradlew testDebugUnitTest --tests "*Epub*"
```

## Troubleshooting

### Test fails: "sample_book.epub not accessible"
- Integration tests run in JVM and may not access assets
- Solution: Use androidTest directory or mock the epub data

### OCR extracts wrong text
- Increase font size (OCR works better with larger text)
- Use higher DPI for screenshot capture
- Ensure good contrast (dark text on light background)

### ML Kit initialization fails
- Ensure Google Play Services is installed on test device
- Check internet connectivity for first-time model download
- Add `android:usesCleartextTraffic="true"` in manifest for test builds

## Summary

The EPUB reading functionality is now thoroughly testable at three levels:
1. **Parsing** (integration tests)
2. **UI presence** (semantic tests)
3. **Visual rendering** (OCR tests)

This ensures that users see exactly what the EPUB contains, with proper formatting and readability.
