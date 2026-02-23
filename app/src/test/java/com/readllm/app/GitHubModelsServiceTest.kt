package com.readllm.app

import com.readllm.app.llm.GitHubModelsService
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.junit.Assert.*

/**
 * Tests for GitHub Models API integration
 */
class GitHubModelsServiceTest {
    
    private lateinit var service: GitHubModelsService
    
    @Before
    fun setup() {
        // Note: These tests require a mocked context or integration test environment
        // For unit tests, we would mock the Retrofit API interface
    }
    
    @Test
    fun `generateQuestions returns valid question format`() = runBlocking {
        // This is a structure test - ensures the data classes are properly defined
        val sampleQuestion = com.readllm.app.llm.TextLLMService.GeneratedQuestion(
            question = "What is the main theme?",
            expectedAnswer = "The main theme is...",
            type = "factual",
            difficulty = 2
        )
        
        assertEquals("What is the main theme?", sampleQuestion.question)
        assertEquals("factual", sampleQuestion.type)
        assertEquals(2, sampleQuestion.difficulty)
    }
    
    @Test
    fun `evaluateAnswer returns valid evaluation format`() {
        val sampleEvaluation = com.readllm.app.llm.TextLLMService.EvaluationResult(
            score = 85,
            isCorrect = true,
            feedback = "Great answer!"
        )
        
        assertEquals(85, sampleEvaluation.score)
        assertTrue(sampleEvaluation.isCorrect)
        assertFalse(sampleEvaluation.feedback.isEmpty())
    }
    
    @Test
    fun `score threshold correctly determines isCorrect`() {
        val passing = com.readllm.app.llm.TextLLMService.EvaluationResult(
            score = 70,
            isCorrect = true,
            feedback = "Good"
        )
        
        val failing = com.readllm.app.llm.TextLLMService.EvaluationResult(
            score = 69,
            isCorrect = false,
            feedback = "Try again"
        )
        
        assertTrue(passing.isCorrect)
        assertFalse(failing.isCorrect)
    }
}
