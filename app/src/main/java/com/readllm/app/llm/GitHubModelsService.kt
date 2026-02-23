package com.readllm.app.llm

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.readllm.app.auth.GitHubAuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * GitHub Models API Service
 * 
 * Provides access to various AI models through GitHub Models Marketplace.
 * Free tier includes models like GPT-4o-mini, Llama 3, Phi-3, etc.
 * 
 * Documentation: https://github.com/marketplace/models
 * 
 * Supported models:
 * - gpt-4o-mini (OpenAI) - Recommended for Q&A
 * - Meta-Llama-3-8B-Instruct
 * - Phi-3-medium-instruct
 * - Mistral-7B-Instruct
 * 
 * Benefits:
 * - No model download required
 * - State-of-the-art quality
 * - Free tier available
 * - Automatic updates
 */
class GitHubModelsService(private val context: Context) {
    
    private val authService = GitHubAuthService(context)
    
    companion object {
        private const val BASE_URL = "https://models.inference.ai.azure.com/"
        private const val DEFAULT_MODEL = "gpt-4o-mini"  // Free tier model
        private const val TIMEOUT_SECONDS = 60L
    }
    
    // Retrofit API interface
    private interface GitHubModelsAPI {
        @POST("chat/completions")
        suspend fun createChatCompletion(
            @Header("Authorization") authorization: String,
            @Body request: ChatCompletionRequest
        ): Response<ChatCompletionResponse>
    }
    
    // Request/Response data classes
    data class ChatCompletionRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        @SerializedName("max_tokens") val maxTokens: Int = 1000
    )
    
    data class Message(
        val role: String,  // "system", "user", or "assistant"
        val content: String
    )
    
    data class ChatCompletionResponse(
        val id: String,
        val choices: List<Choice>,
        val usage: Usage? = null
    )
    
    data class Choice(
        val message: Message,
        @SerializedName("finish_reason") val finishReason: String
    )
    
    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int,
        @SerializedName("completion_tokens") val completionTokens: Int,
        @SerializedName("total_tokens") val totalTokens: Int
    )
    
    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val api: GitHubModelsAPI by lazy {
        retrofit.create(GitHubModelsAPI::class.java)
    }
    
    /**
     * Generate text completion using GitHub Models
     */
    suspend fun generateCompletion(
        systemPrompt: String,
        userPrompt: String,
        model: String = DEFAULT_MODEL,
        temperature: Double = 0.7,
        maxTokens: Int = 1000
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get access token
            val token = authService.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not authenticated. Please sign in with GitHub."))
            
            // Build request
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userPrompt)
                ),
                temperature = temperature,
                maxTokens = maxTokens
            )
            
            // Make API call
            val response = api.createChatCompletion(
                authorization = "Bearer $token",
                request = request
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("No content in response"))
                }
            } else {
                Result.failure(Exception("API error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("GitHubModelsService", "Error generating completion", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate comprehension questions
     */
    suspend fun generateQuestions(
        chapterContent: String,
        numQuestions: Int = 1
    ): Result<List<TextLLMService.GeneratedQuestion>> = withContext(Dispatchers.IO) {
        // Truncate content if too long
        val truncatedContent = if (chapterContent.length > 3000) {
            chapterContent.take(3000) + "..."
        } else {
            chapterContent
        }
        
        val systemPrompt = """
            You are an expert reading comprehension quiz generator. 
            Generate high-quality questions that test deep understanding of the text.
        """.trimIndent()
        
        val userPrompt = """
            Based on the following text, generate exactly $numQuestions comprehension question(s).
            
            TEXT:
            $truncatedContent
            
            Return your response as a JSON array with this exact format:
            [
              {
                "question": "What is the main topic discussed?",
                "expectedAnswer": "Brief expected answer based on the text",
                "type": "factual",
                "difficulty": 2
              }
            ]
            
            Rules:
            - Generate exactly $numQuestions question(s)
            - Questions must be answerable from the text
            - Expected answers should be 1-3 sentences
            - Type can be: "factual", "conceptual", or "inference"
            - Difficulty: 1-5 (use 2-3 for most questions)
            - Return ONLY the JSON array, no additional text
        """.trimIndent()
        
        val result = generateCompletion(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            temperature = 0.8,
            maxTokens = 800
        )
        
        result.fold(
            onSuccess = { response ->
                try {
                    // Parse JSON response
                    val questions = parseQuestionsFromJSON(response)
                    Result.success(questions)
                } catch (e: Exception) {
                    android.util.Log.e("GitHubModelsService", "Error parsing questions", e)
                    Result.failure(e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
    
    /**
     * Evaluate user answer
     */
    suspend fun evaluateAnswer(
        question: String,
        userAnswer: String,
        chapterContent: String
    ): Result<TextLLMService.EvaluationResult> = withContext(Dispatchers.IO) {
        val truncatedContent = if (chapterContent.length > 2000) {
            chapterContent.take(2000) + "..."
        } else {
            chapterContent
        }
        
        val systemPrompt = """
            You are a fair and encouraging reading comprehension evaluator.
            Grade student answers based on accuracy and understanding.
        """.trimIndent()
        
        val userPrompt = """
            Evaluate this student's answer based on the chapter text.
            
            CHAPTER TEXT:
            $truncatedContent
            
            QUESTION:
            $question
            
            STUDENT'S ANSWER:
            $userAnswer
            
            Return a JSON object with this exact format:
            {
              "score": 85,
              "isCorrect": true,
              "feedback": "Good answer! You correctly identified the main concept..."
            }
            
            Grading criteria:
            - Score: 0-100 (0=wrong, 50=partial, 70+=mostly correct, 90+=excellent)
            - isCorrect: true if score >= 70
            - feedback: 1-2 encouraging sentences with specific feedback
            
            Be fair and give partial credit. Return ONLY the JSON object.
        """.trimIndent()
        
        val result = generateCompletion(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            temperature = 0.3,  // Lower temperature for consistent grading
            maxTokens = 300
        )
        
        result.fold(
            onSuccess = { response ->
                try {
                    val evaluation = parseEvaluationFromJSON(response)
                    Result.success(evaluation)
                } catch (e: Exception) {
                    android.util.Log.e("GitHubModelsService", "Error parsing evaluation", e)
                    Result.failure(e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
    
    /**
     * Parse questions from JSON response
     */
    private fun parseQuestionsFromJSON(json: String): List<TextLLMService.GeneratedQuestion> {
        try {
            // Extract JSON array
            val jsonStart = json.indexOf('[')
            val jsonEnd = json.lastIndexOf(']') + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw Exception("No JSON array found in response")
            }
            
            val jsonString = json.substring(jsonStart, jsonEnd)
            val jsonArray = org.json.JSONArray(jsonString)
            
            val questions = mutableListOf<TextLLMService.GeneratedQuestion>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                questions.add(
                    TextLLMService.GeneratedQuestion(
                        question = obj.getString("question"),
                        expectedAnswer = obj.getString("expectedAnswer"),
                        type = obj.optString("type", "factual"),
                        difficulty = obj.optInt("difficulty", 2)
                    )
                )
            }
            
            return questions
        } catch (e: Exception) {
            throw Exception("Failed to parse questions: ${e.message}", e)
        }
    }
    
    /**
     * Parse evaluation from JSON response
     */
    private fun parseEvaluationFromJSON(json: String): TextLLMService.EvaluationResult {
        try {
            // Extract JSON object
            val jsonStart = json.indexOf('{')
            val jsonEnd = json.lastIndexOf('}') + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw Exception("No JSON object found in response")
            }
            
            val jsonString = json.substring(jsonStart, jsonEnd)
            val obj = org.json.JSONObject(jsonString)
            
            return TextLLMService.EvaluationResult(
                score = obj.optInt("score", 50),
                isCorrect = obj.optBoolean("isCorrect", false),
                feedback = obj.optString("feedback", "Your answer has been evaluated.")
            )
        } catch (e: Exception) {
            throw Exception("Failed to parse evaluation: ${e.message}", e)
        }
    }
}
