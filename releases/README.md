# ReadLLM v1.0 - Release Notes

## 📱 Download

**[readllm-v1.0-release.apk](readllm-v1.0-release.apk)** (92 MB)

**Note:** This is an unsigned APK. You may need to enable "Install from Unknown Sources" in your Android settings.

---

## 🎉 What's New in v1.0

### 🤖 GitHub OAuth for AI-Powered Q&A
- **No more large downloads!** Use GitHub Models API instead of downloading 2-3GB models
- Access to **GPT-4o-mini, Llama 3, Phi-3** and more
- Better quality questions and answer evaluation
- Free tier available through GitHub
- Automatic fallback to local mode when offline

### 📖 Improved Reading Experience
- **Fixed Table of Contents** - No more repetitive book titles
- **Enhanced Swipe Navigation** - Easier left/right swipes to change chapters (200px threshold)
- **Removed navigation buttons** - Cleaner, immersive reading with swipe + ToC navigation
- Better gesture handling to prevent scroll conflicts

### 🎨 Beautiful Library UI (ReadEra Style)
- **Grid view by default** with prominent book covers
- Full-cover cards with gradient overlays
- Progress badges displayed on covers (e.g., "45%")
- Favorite indicators on book thumbnails
- More books visible at once

### ✅ Comprehensive Testing
- Full test suite for all new features
- UI tests for navigation and library
- Integration tests for Q&A functionality

---

## 📋 Features

### Reading Features
- ✨ EPUB file support
- 📚 Table of contents navigation
- 👆 Swipe gestures for chapter navigation
- 🎨 Customizable themes and colors
- 📝 Bookmarks and highlights
- 🔊 Text-to-speech (read aloud)
- 👁️ Advanced reading modes:
  - RSVP speed reading
  - Paragraph focus mode
  - Reading ruler
  - Bionic reading
  - Horizontal limiter
  - Perception expander

### AI Features
- 🤖 Comprehension quizzes at chapter endings
- 💬 AI-powered answer evaluation
- 📊 Progress dashboard
- 🎯 Adaptive difficulty based on performance
- ☁️ Cloud-powered via GitHub Models API
- 📴 Offline fallback mode

### Library Management
- 📱 Grid and list view modes
- 🔍 Search and filter
- ⭐ Favorites
- 📊 Reading progress tracking
- 🏷️ Status tracking (Unread, Reading, Finished)
- 📁 Multiple sort options

---

## 🚀 Getting Started

### Installation

1. **Download** the APK from the link above
2. **Enable** "Install from Unknown Sources" on your Android device:
   - Go to Settings → Security
   - Enable "Unknown Sources" or "Install unknown apps"
3. **Install** the APK
4. **Open** ReadLLM and start reading!

### Optional: GitHub OAuth Setup (for AI Features)

To get the best AI-powered questions:

1. **Sign in with GitHub** from the app settings
2. The app will authorize with GitHub Models API
3. Enjoy high-quality comprehension questions!

**Without GitHub OAuth:** The app still works with basic questions or local AI model (if downloaded).

See [GITHUB_OAUTH_SETUP.md](../GITHUB_OAUTH_SETUP.md) for detailed setup instructions.

---

## 📱 Requirements

- **Android 8.0 (API 26)** or higher
- **92 MB** storage space for app
- **Internet connection** (optional, for GitHub Models API)
- **Storage permission** for importing EPUB files

---

## 🐛 Known Issues

1. **Unsigned APK Warning** - You'll see a warning during installation because the APK is not signed. This is normal for development builds.
2. **Book covers require EPUB metadata** - Some EPUBs may not have embedded cover images
3. **GitHub OAuth requires setup** - Developers need to configure CLIENT_ID for OAuth to work

---

## 🔄 Upgrading from Previous Version

If you had a previous version installed:

1. Uninstall the old version
2. Install this new APK
3. Your books and settings should be preserved (backed up via Android Auto Backup)

---

## 📚 Documentation

- [CHANGELOG.md](../CHANGELOG.md) - Detailed changelog
- [GITHUB_OAUTH_SETUP.md](../GITHUB_OAUTH_SETUP.md) - GitHub OAuth setup guide
- [README.md](../README.md) - Project documentation

---

## 🛠️ For Developers

### Building from Source

```bash
git clone https://github.com/yourusername/readllm.git
cd readllm
./gradlew assembleRelease
```

### Running Tests

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # UI tests
```

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

---

## 📝 License

[Your License Here]

---

## 💬 Support

- **Issues:** Report bugs on [GitHub Issues](https://github.com/yourusername/readllm/issues)
- **Discussions:** Ask questions in [Discussions](https://github.com/yourusername/readllm/discussions)

---

## 🙏 Credits

Built with:
- Jetpack Compose
- GitHub Models API
- MediaPipe LLM Inference
- Retrofit
- Coil
- AppAuth

---

**Enjoy reading with AI-powered comprehension! 📚✨**
