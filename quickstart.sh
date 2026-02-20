#!/bin/bash

# ReadLLM Project Quick Start Script
# This script helps you get started with the ReadLLM project

echo "═══════════════════════════════════════════════════════"
echo "  ReadLLM - Accessible eBook Reader with AI Read-Aloud"
echo "═══════════════════════════════════════════════════════"
echo ""

# Check if Android Studio is installed
if command -v studio &> /dev/null; then
    echo "✅ Android Studio found"
else
    echo "❌ Android Studio not found"
    echo "   Please install Android Studio from:"
    echo "   https://developer.android.com/studio"
    echo ""
fi

# Check if Java is installed
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java found: $JAVA_VERSION"
else
    echo "❌ Java not found"
    echo "   Please install JDK 17 or higher"
    echo ""
fi

echo ""
echo "Project Information:"
echo "─────────────────────────────────────────────────────"
echo "📁 Project: ReadLLM"
echo "📱 Platform: Android"
echo "🔧 Min SDK: 26 (Android 8.0)"
echo "🎯 Target SDK: 34 (Android 14)"
echo "💾 Language: Kotlin"
echo "🎨 UI: Jetpack Compose + Material 3"
echo ""

echo "Key Features (MVP):"
echo "─────────────────────────────────────────────────────"
echo "✅ EPUB Reader"
echo "✅ OCR (Google ML Kit)"
echo "✅ AI Visual Explanations (Rule-based + TFLite ready)"
echo "✅ Text-to-Speech Read-Aloud"
echo "✅ Book Library with Progress Tracking"
echo ""

echo "Documentation:"
echo "─────────────────────────────────────────────────────"
echo "📖 README.md          - Main documentation & TODO list"
echo "🚀 GETTING_STARTED.md - Developer setup guide"
echo "📊 MVP_SUMMARY.md     - What's been built"
echo "🏗️  ARCHITECTURE.md    - System design & diagrams"
echo "📋 CHECKLIST.md       - Development roadmap"
echo "📁 FILE_INDEX.md      - Complete file listing"
echo ""

echo "Quick Start Options:"
echo "─────────────────────────────────────────────────────"
echo ""
echo "1) Open in Android Studio"
echo "   - File -> Open -> Select this directory"
echo "   - Wait for Gradle sync"
echo "   - Click Run (▶️)"
echo ""
echo "2) Build from command line"
echo "   ./gradlew build"
echo ""
echo "3) Install on device"
echo "   ./gradlew installDebug"
echo ""
echo "4) Read documentation"
echo "   cat README.md"
echo "   cat GETTING_STARTED.md"
echo ""

# Check if we should proceed with setup
read -p "Do you want to build the project now? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Building project..."
    echo "─────────────────────────────────────────────────────"
    
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        ./gradlew build
    else
        echo "❌ Gradle wrapper not found."
        echo "   Please open the project in Android Studio first."
    fi
else
    echo ""
    echo "Setup skipped. You can run this script again anytime!"
fi

echo ""
echo "Next Steps:"
echo "─────────────────────────────────────────────────────"
echo "1. Read GETTING_STARTED.md for detailed setup"
echo "2. Check CHECKLIST.md for development roadmap"
echo "3. Review TODO items in README.md"
echo ""
echo "Important TODOs (from README):"
echo "  • PDF to EPUB conversion"
echo "  • Reading history & statistics"
echo "  • Integrate actual vision-language model"
echo ""
echo "For help and issues:"
echo "  - Check documentation in .md files"
echo "  - Review ARCHITECTURE.md for system design"
echo "  - See GETTING_STARTED.md for common issues"
echo ""
echo "Happy coding! 🚀"
echo "═══════════════════════════════════════════════════════"
