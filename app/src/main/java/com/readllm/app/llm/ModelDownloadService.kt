package com.readllm.app.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service for downloading AI models for on-device inference
 * 
 * Downloads Gemma 2B-IT model from Hugging Face for AI quiz generation
 */
class ModelDownloadService(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelDownloadService"
        
        // Model download URL - Gemma 2B IT GPU INT4 quantized
        // Using Hugging Face mirror for easier access (no auth required)
        private const val MODEL_URL = "https://huggingface.co/google/gemma-2b-it/resolve/main/model.bin"
        
        // Alternative: Use a smaller model for testing/faster download
        private const val SMALL_MODEL_URL = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        
        private const val MODEL_FILENAME = "gemma-2b-it-gpu-int4.bin"
        private const val CHUNK_SIZE = 8192 // 8KB chunks for smooth progress
    }
    
    sealed class DownloadStatus {
        object Idle : DownloadStatus()
        object Checking : DownloadStatus()
        object AlreadyDownloaded : DownloadStatus()
        data class Downloading(val progress: Float, val downloadedMB: Float, val totalMB: Float) : DownloadStatus()
        object Success : DownloadStatus()
        data class Error(val message: String) : DownloadStatus()
    }
    
    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus
    
    /**
     * Check if model is already downloaded
     */
    fun isModelDownloaded(): Boolean {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        val exists = modelFile.exists() && modelFile.length() > 0
        Log.d(TAG, "Model exists: $exists, path: ${modelFile.absolutePath}")
        return exists
    }
    
    /**
     * Get model file path
     */
    fun getModelPath(): String? {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        return if (modelFile.exists()) modelFile.absolutePath else null
    }
    
    /**
     * Delete downloaded model
     */
    suspend fun deleteModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(context.filesDir, MODEL_FILENAME)
            val deleted = modelFile.delete()
            if (deleted) {
                _downloadStatus.value = DownloadStatus.Idle
                Log.d(TAG, "Model deleted successfully")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model: ${e.message}", e)
            false
        }
    }
    
    /**
     * Download model from Hugging Face
     * 
     * Note: This downloads directly to app's internal storage (filesDir)
     * so it's accessible to MediaPipe LLM Inference
     */
    suspend fun downloadModel(useSmallModel: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            _downloadStatus.value = DownloadStatus.Checking
            
            // Check if already downloaded
            if (isModelDownloaded()) {
                Log.d(TAG, "Model already downloaded")
                _downloadStatus.value = DownloadStatus.AlreadyDownloaded
                return@withContext true
            }
            
            val downloadUrl = if (useSmallModel) SMALL_MODEL_URL else MODEL_URL
            Log.d(TAG, "Starting download from: $downloadUrl")
            
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "ReadLLM-Android-App")
            
            // Follow redirects (Hugging Face uses them)
            connection.instanceFollowRedirects = true
            
            connection.connect()
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val error = "Download failed: HTTP $responseCode"
                Log.e(TAG, error)
                _downloadStatus.value = DownloadStatus.Error(error)
                return@withContext false
            }
            
            val totalBytes = connection.contentLength.toLong()
            val totalMB = totalBytes / (1024f * 1024f)
            
            Log.d(TAG, "Download started. Total size: ${"%.2f".format(totalMB)} MB")
            
            // Create temp file first, then rename on success
            val tempFile = File(context.filesDir, "$MODEL_FILENAME.tmp")
            val modelFile = File(context.filesDir, MODEL_FILENAME)
            
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0f
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        val progress = totalBytesRead.toFloat() / totalBytes
                        val downloadedMB = totalBytesRead / (1024f * 1024f)
                        
                        // Update progress every 1% to avoid too many updates
                        if (progress - lastProgressUpdate >= 0.01f || progress >= 1f) {
                            _downloadStatus.value = DownloadStatus.Downloading(
                                progress = progress,
                                downloadedMB = downloadedMB,
                                totalMB = totalMB
                            )
                            lastProgressUpdate = progress
                            Log.d(TAG, "Download progress: ${"%.1f".format(progress * 100)}%")
                        }
                    }
                }
            }
            
            connection.disconnect()
            
            // Rename temp file to final name
            if (tempFile.renameTo(modelFile)) {
                Log.d(TAG, "Download completed successfully")
                _downloadStatus.value = DownloadStatus.Success
                true
            } else {
                val error = "Failed to save downloaded model"
                Log.e(TAG, error)
                tempFile.delete()
                _downloadStatus.value = DownloadStatus.Error(error)
                false
            }
            
        } catch (e: Exception) {
            val error = "Download error: ${e.message}"
            Log.e(TAG, error, e)
            _downloadStatus.value = DownloadStatus.Error(e.message ?: "Unknown error")
            
            // Clean up temp file if exists
            File(context.filesDir, "$MODEL_FILENAME.tmp").delete()
            
            false
        }
    }
    
    /**
     * Cancel ongoing download
     */
    fun cancelDownload() {
        // In a real implementation, we'd need to track the connection and close it
        // For now, we'll just reset the status
        _downloadStatus.value = DownloadStatus.Idle
        
        // Clean up temp file
        File(context.filesDir, "$MODEL_FILENAME.tmp").delete()
    }
}
