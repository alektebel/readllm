package com.readllm.app.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRService {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    data class OCRResult(
        val text: String,
        val confidence: Float,
        val isEquation: Boolean,
        val isTable: Boolean
    )
    
    suspend fun extractTextFromImage(bitmap: Bitmap): OCRResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val result = recognizer.process(image).await()
            val extractedText = result.text
            
            // Heuristic detection for equations and tables
            val isEquation = detectEquation(extractedText)
            val isTable = detectTable(extractedText, result.textBlocks.size)
            
            val confidence = result.textBlocks
                .flatMap { it.lines }
                .mapNotNull { it.confidence }
                .average()
                .toFloat()
            
            OCRResult(
                text = extractedText,
                confidence = if (confidence.isNaN()) 0f else confidence,
                isEquation = isEquation,
                isTable = isTable
            )
        } catch (e: Exception) {
            OCRResult("", 0f, false, false)
        }
    }
    
    private fun detectEquation(text: String): Boolean {
        // Simple heuristic: contains mathematical symbols
        val mathSymbols = listOf("∫", "∑", "√", "π", "∞", "≈", "≠", "≤", "≥", "×", "÷", "±")
        val hasComplexMath = mathSymbols.any { text.contains(it) }
        
        // Check for patterns like "x = y" or variables with subscripts
        val hasMathPattern = Regex("[a-zA-Z]\\s*[=<>]\\s*[a-zA-Z0-9]").containsMatchIn(text) ||
                             Regex("\\d+\\s*[+\\-*/]\\s*\\d+").containsMatchIn(text) ||
                             Regex("[a-zA-Z]_\\d").containsMatchIn(text)
        
        return hasComplexMath || hasMathPattern
    }
    
    private fun detectTable(text: String, blockCount: Int): Boolean {
        // Heuristic: multiple columns or grid-like structure
        val lines = text.lines()
        if (lines.size < 2) return false
        
        // Check for consistent spacing/tabs across lines
        val hasConsistentStructure = lines.count { it.contains("\\s{3,}".toRegex()) } > lines.size / 2
        val hasTabular = lines.count { it.split("\\s{2,}".toRegex()).size > 2 } > lines.size / 2
        
        return hasConsistentStructure || hasTabular || blockCount > 4
    }
    
    fun cleanup() {
        recognizer.close()
    }
}
