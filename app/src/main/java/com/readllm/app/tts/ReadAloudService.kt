package com.readllm.app.tts

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.readllm.app.llm.LLMService
import com.readllm.app.ocr.OCRService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class ReadAloudService(
    private val context: Context,
    private val onStatusChange: (ReadAloudStatus) -> Unit
) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val ocrService = OCRService()
    private val llmService = LLMService(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    sealed class ReadAloudStatus {
        object Idle : ReadAloudStatus()
        object Speaking : ReadAloudStatus()
        object Paused : ReadAloudStatus()
        data class Error(val message: String) : ReadAloudStatus()
        data class Progress(val position: Int) : ReadAloudStatus()
    }
    
    data class ContentSegment(
        val text: String? = null,
        val image: Bitmap? = null,
        val position: Int
    )
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
                isInitialized = true
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onStatusChange(ReadAloudStatus.Speaking)
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        utteranceId?.toIntOrNull()?.let { position ->
                            onStatusChange(ReadAloudStatus.Progress(position))
                        }
                    }
                    
                    override fun onError(utteranceId: String?) {
                        onStatusChange(ReadAloudStatus.Error("Speech synthesis error"))
                    }
                })
            } else {
                onStatusChange(ReadAloudStatus.Error("TTS initialization failed"))
            }
        }
    }
    
    /**
     * Reads content aloud, including explanations for visual elements
     */
    fun readAloud(segments: List<ContentSegment>, startPosition: Int = 0) {
        if (!isInitialized) {
            onStatusChange(ReadAloudStatus.Error("TTS not initialized"))
            return
        }
        
        scope.launch {
            for (i in startPosition until segments.size) {
                val segment = segments[i]
                
                when {
                    segment.text != null -> {
                        // Clean HTML tags from text before speaking
                        val cleanText = cleanHtmlForSpeech(segment.text)
                        if (cleanText.isNotBlank()) {
                            speakText(cleanText, i.toString())
                        }
                    }
                    
                    segment.image != null -> {
                        // Process image and speak explanation
                        val explanation = processImage(segment.image)
                        if (explanation.isNotBlank()) {
                            speakText(explanation, i.toString())
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Clean HTML content for speech synthesis
     * Removes all HTML tags and decodes entities
     */
    private fun cleanHtmlForSpeech(html: String): String {
        // Remove all HTML tags
        var cleaned = html
            .replace(Regex("<[^>]+>"), " ")  // Remove all tags
            .replace(Regex("\\s+"), " ")      // Normalize whitespace
            .trim()
        
        // Decode HTML entities
        cleaned = cleaned
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&lsquo;", "'")
            .replace("&rsquo;", "'")
            .replace("&ldquo;", """)
            .replace("&rdquo;", """)
            .replace("&hellip;", "...")
            // Decode numeric HTML entities
            .replace(Regex("&#x([0-9a-fA-F]+);")) { matchResult ->
                matchResult.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: matchResult.value
            }
            .replace(Regex("&#(\\d+);")) { matchResult ->
                matchResult.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: matchResult.value
            }
        
        return cleaned
    }
    
    private suspend fun processImage(bitmap: Bitmap): String {
        // Extract text using OCR
        val ocrResult = ocrService.extractTextFromImage(bitmap)
        
        // Generate explanation using LLM
        val explanation = llmService.explainVisualContent(
            bitmap = bitmap,
            ocrText = ocrResult.text,
            isEquation = ocrResult.isEquation,
            isTable = ocrResult.isTable,
            context = "reading book content"
        )
        
        return explanation.explanation
    }
    
    private fun speakText(text: String, utteranceId: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId)
    }
    
    fun pause() {
        tts?.stop()
        onStatusChange(ReadAloudStatus.Paused)
    }
    
    fun resume(segments: List<ContentSegment>, position: Int) {
        readAloud(segments, position)
    }
    
    fun stop() {
        tts?.stop()
        onStatusChange(ReadAloudStatus.Idle)
    }
    
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
    
    fun cleanup() {
        tts?.stop()
        tts?.shutdown()
        ocrService.cleanup()
        llmService.cleanup()
    }
}
