# LLM Model Setup for ReadLLM

## Overview

ReadLLM uses an on-device LLM (Large Language Model) to generate comprehension questions and evaluate user answers. This provides a personalized, AI-powered quiz experience without requiring an internet connection.

## Required Model

**Model**: Gemma 2B-IT (Instruction-Tuned) - GPU INT4 Quantized  
**Size**: ~1.5 GB  
**Source**: Google via Kaggle  

## How to Download and Install

### Step 1: Download the Model

1. Go to Kaggle Models: https://www.kaggle.com/models/google/gemma/tfLite/
2. Find **Gemma 2B-IT GPU INT4** variant (recommended for mobile)
3. Download the model file (should be named `gemma-2b-it-gpu-int4.bin` or similar)

### Step 2: Place the Model in Your Project

1. Locate your project directory: `/home/diego/Development/archive/readllm/`
2. Navigate to: `app/src/main/assets/`
3. Create a `models` directory if it doesn't exist:
   ```bash
   mkdir -p app/src/main/assets/models
   ```
4. Copy the downloaded model file into `app/src/main/assets/models/`
5. Rename the file to exactly: `gemma-2b-it-gpu-int4.bin`

Your final path should be:
```
/home/diego/Development/archive/readllm/app/src/main/assets/models/gemma-2b-it-gpu-int4.bin
```

### Step 3: Build and Run

The app will automatically load the model when it starts. If the model is not found, the app will fall back to rule-based question generation (which is less intelligent but still functional).

## Alternative Models

If Gemma 2B-IT is too large or doesn't work well on your device, you can try these alternatives:

### TinyLlama 1.1B
- **Size**: ~600 MB
- **Source**: https://github.com/jzhang38/TinyLlama
- **Note**: Smaller, faster, but less capable

### Phi-2 (2.7B)
- **Size**: ~1.8 GB  
- **Source**: Microsoft via HuggingFace
- **Note**: Similar quality to Gemma, slightly larger

To use a different model:
1. Download the TFLite version of the model
2. Place it in `app/src/main/assets/models/`
3. Update `TextLLMService.kt` line 38 to use the new model filename:
   ```kotlin
   private const val MODEL_PATH = "your-model-name.bin"
   ```

## Testing the LLM

To verify the LLM is working:

1. Build and run the app on an Android device/emulator (API 26+)
2. Open a book and read to the end of a chapter
3. A quiz should appear with AI-generated questions
4. Answer the question and see AI-powered evaluation

If you see generic questions like "What is the main topic?", the LLM model is not loaded. Check:
- The model file is in the correct location
- The filename matches exactly: `gemma-2b-it-gpu-int4.bin`
- Check Android Logcat for error messages (filter for "TextLLMService")

## Model Performance

**Expected behavior:**
- **Initialization**: 2-5 seconds on first app launch
- **Question generation**: 5-15 seconds (shown with loading screen)
- **Answer evaluation**: 3-8 seconds (shown with "Evaluating..." button)

The loading screens ensure users aren't confused while the AI is thinking.

## Fallback Mode

If the model fails to load or is unavailable:
- **Question Generation**: Falls back to generic questions about the chapter
- **Answer Evaluation**: Uses simple word-count heuristics
- **User Experience**: Still functional, just less intelligent

Check Android Logcat for warnings:
```
W/TextLLMService: LLM not initialized, returning fallback questions
W/TextLLMService: LLM not initialized, using fallback evaluation
```

## Troubleshooting

### "Model file not found"
- Verify the file path: `app/src/main/assets/models/gemma-2b-it-gpu-int4.bin`
- Check the filename matches exactly (case-sensitive)

### "Out of memory" errors
- Try the smaller TinyLlama 1.1B model
- Close other apps to free up RAM
- Use a device with at least 4GB RAM

### Model loads but generates gibberish
- The model file may be corrupted, re-download it
- Ensure you downloaded the **TFLite** version (not PyTorch or ONNX)

### Slow inference (>30 seconds)
- Make sure you downloaded the **GPU INT4** quantized version
- Try running on a newer device with better GPU
- Check that MediaPipe is using GPU acceleration

## Privacy & Security

**All inference happens on-device:**
- No chapter content is sent to external servers
- No user answers are transmitted online
- Complete privacy for your reading data
- Works 100% offline

## Technical Details

**Framework**: MediaPipe LLM Inference API  
**Language**: Kotlin  
**Min SDK**: 26 (Android 8.0)  
**Required RAM**: 3-4 GB recommended  
**Storage**: 1.5 GB for model + 100 MB for app  

The TextLLMService handles:
1. Model loading and initialization
2. Prompt engineering for question generation
3. JSON parsing of LLM responses
4. Semantic answer evaluation
5. Graceful fallbacks when model is unavailable

## Further Reading

- [MediaPipe LLM Inference](https://developers.google.com/mediapipe/solutions/genai/llm_inference)
- [Gemma Models](https://ai.google.dev/gemma)
- [TensorFlow Lite for Mobile](https://www.tensorflow.org/lite)
