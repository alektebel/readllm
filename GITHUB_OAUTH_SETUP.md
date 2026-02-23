# GitHub OAuth Setup Guide

## Quick Start

This guide helps you set up GitHub OAuth for AI-powered question generation in ReadLLM.

## Why GitHub OAuth?

Instead of downloading a 2-3GB AI model to your device, ReadLLM can use GitHub Models API to access state-of-the-art AI models like GPT-4o-mini, Llama 3, and Phi-3 for **free**.

### Benefits:
- ✅ No large model downloads
- ✅ Better question quality
- ✅ Faster performance
- ✅ Always up-to-date models
- ✅ Works on any device

## Setup Steps

### Step 1: Create a GitHub OAuth App

1. Go to https://github.com/settings/developers
2. Click **"New OAuth App"**
3. Fill in the details:
   - **Application name:** ReadLLM (or your preferred name)
   - **Homepage URL:** `https://github.com/yourusername/readllm` (or your repo)
   - **Authorization callback URL:** `com.readllm.app://oauth`
4. Click **"Register application"**

### Step 2: Get Your Client ID

After creating the app:
1. You'll see your **Client ID** on the app page
2. Copy this ID (you'll need it in the next step)

### Step 3: Update the App

1. Open `app/src/main/java/com/readllm/app/auth/GitHubAuthService.kt`
2. Find this line:
   ```kotlin
   private const val CLIENT_ID = "YOUR_GITHUB_CLIENT_ID"
   ```
3. Replace `YOUR_GITHUB_CLIENT_ID` with your actual Client ID from Step 2:
   ```kotlin
   private const val CLIENT_ID = "Iv1.abc123def456"  // Your actual ID
   ```

### Step 4: Update AndroidManifest.xml

Add the OAuth callback intent filter to your `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    
    <!-- Existing intent filters... -->
    
    <!-- Add this for GitHub OAuth callback -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="com.readllm.app"
              android:host="oauth" />
    </intent-filter>
</activity>
```

### Step 5: Build and Run

```bash
./gradlew clean build
./gradlew installDebug
```

## Using the Feature

### In the App:

1. Open ReadLLM
2. Go to **Settings**
3. Tap **"Sign in with GitHub"**
4. Authorize the app in your browser
5. Return to the app
6. Start reading and enjoy AI-generated questions!

### Testing:

Read any book chapter and when you finish, you should see:
- Higher quality comprehension questions
- Better answer evaluation
- Personalized feedback

## Troubleshooting

### "Not authenticated" Error

**Problem:** Questions fall back to basic mode

**Solutions:**
1. Check that CLIENT_ID is set correctly
2. Verify the callback URL is `com.readllm.app://oauth`
3. Sign in again from Settings
4. Check internet connection

### OAuth Redirect Not Working

**Problem:** After authorizing on GitHub, app doesn't open

**Solutions:**
1. Verify intent filter is added to AndroidManifest.xml
2. Check scheme is `com.readllm.app` (not `http` or `https`)
3. Rebuild the app after manifest changes

### API Rate Limiting

**Problem:** "Too many requests" error

**Solution:** 
- GitHub Models has rate limits on free tier
- Wait a few minutes before trying again
- Consider upgrading to GitHub Pro for higher limits

## Fallback Mode

If GitHub OAuth is not configured or user is offline, the app automatically falls back to:

1. **Local model** (if downloaded) - Gemma 2B-IT
2. **Basic questions** - Simple comprehension prompts

## Security Notes

- Never commit your Client ID to public repositories
- Use environment variables or build configs for production
- The app only requests `read:user` scope (minimal permissions)
- Tokens are stored securely using Android DataStore

## GitHub Models API

### Available Models (Free Tier):
- **gpt-4o-mini** (OpenAI) ← Default, best for Q&A
- **Meta-Llama-3-8B-Instruct**
- **Phi-3-medium-instruct**
- **Mistral-7B-Instruct**

### Switching Models:

In `GitHubModelsService.kt`, change:
```kotlin
private const val DEFAULT_MODEL = "gpt-4o-mini"
```

To:
```kotlin
private const val DEFAULT_MODEL = "Meta-Llama-3-8B-Instruct"
```

## Cost

GitHub Models is **FREE** for:
- Personal use
- Testing and development
- Reasonable usage (rate limits apply)

## Need Help?

- Check the [CHANGELOG.md](CHANGELOG.md) for detailed technical info
- File an issue on GitHub
- Refer to [GitHub Models docs](https://github.com/marketplace/models)

---

**Happy Reading! 📚✨**
