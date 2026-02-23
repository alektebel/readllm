# ReadLLM - Changelog

## Version 1.5.2 (February 24, 2026)

### GitHub OAuth UI Implementation

**What's New:**
- Added complete user interface for GitHub OAuth authentication
- Users can now sign in/out with GitHub directly from the Settings screen
- Visual feedback showing authentication status
- Clear display of GitHub Models API benefits

**New Features:**
1. **GitHub Integration Section in Settings**
   - Shows authentication status with user's GitHub login
   - Displays benefits list when not authenticated
   - Sign In / Sign Out buttons
   - Connected status with checkmark icon
   - Clean, intuitive UI design

2. **OAuth Flow Integration**
   - MainActivity now handles OAuth callbacks
   - Browser-based authentication flow
   - Toast notifications for success/error states
   - Proper error handling throughout the flow

3. **User Experience Improvements**
   - All error messages in MainActivity now shown to users via Toast
   - Success confirmation when importing books
   - Clear feedback for all file operations

**Code Quality Improvements:**
1. **Completed All TODOs:**
   - Implemented time tracking for quiz sessions (QuizRepository)
   - Implemented detailed question type analytics
   - Removed unnecessary LLM service TODO (GitHub API is superior)

2. **Bug Fixes:**
   - Fixed duplicate @Composable annotation in SettingsScreen
   - Fixed question type enum references (VISUAL_CONTENT)
   - Improved type safety in analytics calculations

**Files Modified:**
- `SettingsScreen.kt` - Added GitHubIntegrationSection UI component (158 lines)
- `MainActivity.kt` - Added OAuth launcher, callbacks, and error handling
- `GitHubAuthService.kt` - Added convenience methods for UI integration
- `QuizRepository.kt` - Completed time tracking and type analytics TODOs
- `LLMService.kt` - Clarified local model vs GitHub API usage

**Technical Details:**
- OAuth flow: Settings → Sign In → Browser → Callback → MainActivity → Success/Error Toast
- All tokens stored with AES256-GCM encryption (from v1.1)
- Offline mode works with fallback to local model
- GitHub Models API provides superior question generation when authenticated

**Size:** 93 MB

---

## Version 1.1 (February 23, 2026)

### Critical Security and Stability Fixes

**What's Fixed:**
1. **Security Vulnerabilities:**
   - Fixed XXE (XML External Entity) vulnerability in EPUB parser
   - Implemented encrypted token storage using AES256-GCM
   - Moved CLIENT_ID from hardcoded to BuildConfig
   - Added comprehensive input validation

2. **Stability Issues:**
   - Fixed infinite flow collection causing ANR (Application Not Responding)
   - Fixed OAuth busy-wait loop consuming CPU
   - Moved all I/O operations to background threads
   - Improved book scanning performance

3. **New Security Features:**
   - SecureTokenStorage.kt - Encrypted storage for OAuth tokens
   - Proper key derivation for encryption
   - Secure token deletion on sign out

**Files Modified:**
- `MainActivity.kt` - Fixed infinite flow collection
- `GitHubAuthService.kt` - Fixed busy-wait loop, added secure storage
- `BookScanner.kt` - Moved I/O to background threads
- `EpubReaderService.kt` - Fixed XXE vulnerability
- `build.gradle.kts` - Externalized CLIENT_ID

**Files Added:**
- `SecureTokenStorage.kt` - AES256-GCM encrypted token storage

**Size:** 93 MB

---

## Version 1.0 (Initial Release)

## Recent Updates

### 1. GitHub OAuth Integration for AI-Powered Q&A

**What Changed:**
- Replaced on-device LLM model requirement with GitHub Models API
- Added GitHub OAuth authentication
- Implemented fallback to local model when offline

**Benefits:**
- ✅ No need to download 2-3GB model files
- ✅ Access to state-of-the-art models (GPT-4o-mini, Llama 3, Phi-3)
- ✅ Better question quality and answer evaluation
- ✅ Free tier available through GitHub Models
- ✅ Automatic model updates

**Setup Instructions:**
1. Create a GitHub OAuth App at https://github.com/settings/developers
2. Set callback URL to: `com.readllm.app://oauth`
3. Update `CLIENT_ID` in `GitHubAuthService.kt` with your app's Client ID
4. Users sign in with GitHub to enable AI features
5. Fallback to basic questions if not authenticated

**Files Added:**
- `app/src/main/java/com/readllm/app/auth/GitHubAuthService.kt`
- `app/src/main/java/com/readllm/app/llm/GitHubModelsService.kt`

**Files Modified:**
- `app/src/main/java/com/readllm/app/llm/TextLLMService.kt` - Now tries GitHub API first
- `app/build.gradle.kts` - Added Retrofit, OkHttp, and AppAuth dependencies

---

### 2. Fixed Table of Contents

**What Changed:**
- Top bar now shows only the current chapter title
- Removed redundant book title display
- Cleaner, less cluttered navigation

**Before:**
```
┌─────────────────────────────┐
│ My Book Title               │
│ Chapter 1: Introduction     │ ← Redundant!
└─────────────────────────────┘
```

**After:**
```
┌─────────────────────────────┐
│ Chapter 1: Introduction     │ ← Clean!
└─────────────────────────────┘
```

**Files Modified:**
- `app/src/main/java/com/readllm/app/ReaderActivity.kt:401-412`

---

### 3. Fixed Swipe Navigation

**What Changed:**
- Reduced swipe threshold from 300px to 200px for easier swiping
- Improved gesture detection to prevent scroll conflicts
- Added proper event consumption to avoid interfering with vertical scrolling
- Added drag state tracking for better UX

**Technical Improvements:**
- Events are consumed only when horizontal drag exceeds 50px
- Proper `onDragStart`, `onDragEnd`, and `onDragCancel` handling
- Key-based `pointerInput` modifier for proper recomposition

**Files Modified:**
- `app/src/main/java/com/readllm/app/ReaderActivity.kt:387-602`

---

### 4. Removed Navigation Buttons

**What Changed:**
- Removed "Previous" and "Next" buttons from bottom bar
- Navigation is now exclusively through:
  - Swipe gestures (left/right)
  - Table of Contents
  - Chapter navigation drawer

**Rationale:**
- Cleaner, more immersive reading experience
- Follows modern mobile reading app design patterns
- Swipe navigation is more intuitive and faster

**Files Modified:**
- `app/src/main/java/com/readllm/app/ReaderActivity.kt:496-563`

---

### 5. Enhanced Library UI (ReadEra Style)

**What Changed:**
- Grid view is now the default (like ReadEra)
- Larger, more prominent book covers
- Cover images take full card space with gradient overlay
- Text overlaid on bottom of cover for maximum visual impact
- Progress badges displayed prominently
- Favorite indicator on cover
- Tighter grid spacing for more books visible at once

**Visual Improvements:**
- Card aspect ratio: 0.65 (taller to emphasize covers)
- Gradient overlay for text readability
- Progress percentage badge at top right
- Favorite heart icon at top left
- Author and title overlaid on cover with shadow

**Files Modified:**
- `app/src/main/java/com/readllm/app/ui/library/EnhancedLibraryScreen.kt`
  - Line 59: Changed default view mode to GRID
  - Lines 328-435: Completely redesigned `BookGridItem`

---

### 6. Comprehensive Test Suite

**New Test Files:**

#### Unit Tests
- `GitHubModelsServiceTest.kt` - Tests for GitHub API integration
  - Question format validation
  - Evaluation result validation
  - Score threshold logic

#### Android Instrumentation Tests
- `ChapterNavigationTest.kt` - Table of Contents tests
  - Chapter title display
  - Current chapter highlighting
  - No title repetition
  
- `LibraryScreenTest.kt` - Library UI tests
  - Default grid view
  - Book cover display
  - Progress badges
  - Favorite indicators
  - View mode toggle

- `SwipeAndQuizTest.kt` - Navigation and Quiz tests
  - Swipe navigation functionality
  - Navigation button removal
  - Q&A integration
  - Fallback behavior

**Running Tests:**
```bash
# Unit tests
./gradlew test

# Android instrumentation tests
./gradlew connectedAndroidTest
```

---

## Migration Guide

### For Users

1. **Update the app** to the latest version
2. **Sign in with GitHub** (optional but recommended)
   - Go to Settings → Sign in with GitHub
   - Authorize the app
   - Enjoy better AI-powered questions!
3. **Try swipe navigation** - swipe left/right to change chapters
4. **Explore the new grid view** - your book covers are now front and center

### For Developers

1. **Update dependencies:**
   ```bash
   ./gradlew clean build
   ```

2. **Configure GitHub OAuth:**
   - Create OAuth app at https://github.com/settings/developers
   - Update `CLIENT_ID` in `GitHubAuthService.kt`

3. **Test the changes:**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

---

## Technical Details

### New Dependencies

```kotlin
// Networking for GitHub API
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// OAuth for GitHub authentication
implementation("net.openid:appauth:0.11.1")
```

### API Integration Flow

```
User reads chapter
      ↓
Chapter ends
      ↓
Quiz triggered
      ↓
TextLLMService.generateQuestions()
      ↓
Try GitHub Models API
      ↓
   Success? ──No──→ Try local model
      ↓                    ↓
     Yes              Success? ──No──→ Fallback questions
      ↓                    ↓
   GPT-4o-mini          Gemma 2B
      ↓                    ↓
   Display questions ←─────┘
```

### Swipe Gesture Logic

```kotlin
detectHorizontalDragGestures(
    onDragStart = { 
        isDragging = true 
        dragOffset = 0f 
    },
    onHorizontalDrag = { change, dragAmount ->
        dragOffset += dragAmount
        if (abs(dragOffset) > 50f) {
            change.consume()  // Prevent scroll conflict
        }
    },
    onDragEnd = {
        if (abs(dragOffset) > threshold) {
            // Navigate to next/previous chapter
        }
        reset()
    }
)
```

---

## Known Issues & Limitations

1. **GitHub OAuth requires internet connection**
   - Fallback to local model available
   - Local model requires manual download (2-3GB)

2. **Book covers require EPUB metadata**
   - Some EPUBs may not have cover images
   - Placeholder icon shown when no cover available

3. **Swipe navigation conflicts with text selection**
   - Selecting text may trigger swipe if dragged horizontally
   - Use long-press for text selection to avoid this

---

## Future Enhancements

- [ ] OAuth token refresh handling
- [ ] More granular swipe sensitivity settings
- [ ] Cover image extraction from EPUB files
- [ ] Offline question caching
- [ ] Multiple AI model selection (GPT-4, Llama 3, Phi-3)

---

## Questions or Issues?

Please file an issue on the GitHub repository with:
- App version
- Android version
- Steps to reproduce
- Expected vs actual behavior
