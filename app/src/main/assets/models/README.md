# Model Directory

This directory contains AI models for the ReadLLM app.

## ⚠️ IMPORTANT: Model Required for AI Features

**The app currently needs a text-based LLM model for AI-powered quiz generation.**

### Required Model

**Place the following model in this directory:**

**Filename**: `gemma-2b-it-gpu-int4.bin`  
**Full Path**: `/app/src/main/assets/models/gemma-2b-it-gpu-int4.bin`

### Download Instructions

1. **Go to Kaggle**: https://www.kaggle.com/models/google/gemma/tfLite/
2. **Find**: Gemma 2B-IT GPU INT4 variant
3. **Download**: The model file (~1.5 GB)
4. **Rename**: To exactly `gemma-2b-it-gpu-int4.bin`
5. **Place**: In this directory

### Alternative Models

If Gemma 2B-IT doesn't work for you, try these alternatives:

#### TinyLlama 1.1B
- **Size**: ~600 MB
- **Pros**: Smaller, faster
- **Cons**: Less capable than Gemma
- **Download**: https://github.com/jzhang38/TinyLlama

#### Phi-2 (2.7B)
- **Size**: ~1.8 GB
- **Pros**: Similar quality to Gemma
- **Cons**: Slightly larger
- **Download**: https://huggingface.co/microsoft/phi-2

**To use a different model:**
1. Download the TFLite version
2. Place it in this directory
3. Update `TextLLMService.kt` line 38:
   ```kotlin
   private const val MODEL_PATH = "your-model-filename.bin"
   ```

## What Happens Without the Model?

**The app still works**, but AI features fall back to:
- Generic questions: "What is the main topic of this chapter?"
- Simple answer evaluation based on word count

**With the model:**
- Questions are generated based on actual chapter content
- Answers are evaluated semantically with detailed feedback
- Full AI-powered reading comprehension experience

## Verification

After adding the model, verify it's working:

1. Build and run the app
2. Read to the end of a chapter
3. Check if questions are contextual (not generic)
4. Look at Logcat:
   ```bash
   adb logcat | grep TextLLMService
   ```
   - Should see: "LLM initialized successfully" ✅
   - Should NOT see: "LLM not initialized, returning fallback" ❌

## File Structure

```
app/src/main/assets/models/
├── README.md (this file)
└── gemma-2b-it-gpu-int4.bin (YOU NEED TO ADD THIS)
```

## Model Performance

**Expected behavior with Gemma 2B-IT:**
- **Initialization**: 2-5 seconds (on app startup)
- **Question generation**: 5-15 seconds per quiz
- **Answer evaluation**: 3-8 seconds per answer
- **Memory usage**: ~1.5-2 GB RAM
- **Storage**: ~1.5 GB

## Troubleshooting

### "Model file not found"
- Check the filename is exactly: `gemma-2b-it-gpu-int4.bin`
- Verify it's in the correct directory
- Rebuild the app after adding the model

### "Out of memory"
- Try TinyLlama 1.1B (smaller model)
- Close other apps to free RAM
- Use a device with at least 4GB RAM

### Model loads but generates gibberish
- Re-download the model (file may be corrupted)
- Ensure you downloaded the **TFLite** version (not PyTorch)

## Privacy Note

**All AI inference happens on-device.**  
No chapter content or user answers are sent to external servers.  
The model runs 100% offline after download.

## For More Information

See: `/LLM_SETUP.md` in the project root for detailed setup instructions.
