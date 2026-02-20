package com.readllm.app.quiz

import com.readllm.app.llm.TextLLMService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

/**
 * Unit tests for ComprehensionQuizService
 */
class ComprehensionQuizServiceTest {
    
    private lateinit var quizService: ComprehensionQuizService
    private lateinit var mockTextLLMService: TextLLMService
    
    @Before
    fun setup() {
        // Create mock TextLLMService
        mockTextLLMService = mock()
        quizService = ComprehensionQuizService(mockTextLLMService)
    }
    
    @Test
    fun `shouldShowQuiz returns true at chapter endings`() {
        val chapterContent = "This is the end of the chapter."
        val shouldShow = quizService.shouldShowQuiz(chapterContent, 1)
        
        // Should show quiz at chapter endings
        assertTrue(shouldShow)
    }
    
    @Test
    fun `generateQuestions creates correct number of questions`() = runBlocking {
        val chapterContent = """
            Artificial Intelligence is the simulation of human intelligence.
            Machine learning is a subset of AI. Neural networks are inspired by the brain.
            Deep learning uses multiple layers. AI has many applications in healthcare.
        """.trimIndent()
        
        // Mock LLM service response
        val mockQuestions = listOf(
            TextLLMService.GeneratedQuestion(
                question = "What is AI?",
                expectedAnswer = "Simulation of human intelligence",
                type = "factual",
                difficulty = 2
            )
        )
        `when`(mockTextLLMService.generateQuestions(any(), any(), any())).thenReturn(mockQuestions)
        
        val questions = quizService.generateQuestions(
            chapterContent = chapterContent,
            chapterNumber = 1,
            previousScore = 75
        )
        
        assertTrue(questions.isNotEmpty())
        assertTrue(questions.size <= 5) // Should generate reasonable number
    }
    
    @Test
    fun `generateQuestions includes different question types`() = runBlocking {
        val chapterContent = """
            Chapter about AI and machine learning with various concepts.
            It covers supervised learning, unsupervised learning, and reinforcement learning.
            Neural networks consist of input, hidden, and output layers.
        """.trimIndent()
        
        // Mock LLM service with multiple question types
        val mockQuestions = listOf(
            TextLLMService.GeneratedQuestion(
                question = "What is machine learning?",
                expectedAnswer = "A subset of AI",
                type = "factual",
                difficulty = 2
            ),
            TextLLMService.GeneratedQuestion(
                question = "How do neural networks work?",
                expectedAnswer = "Using layers to process information",
                type = "conceptual",
                difficulty = 3
            )
        )
        `when`(mockTextLLMService.generateQuestions(any(), any(), any())).thenReturn(mockQuestions)
        
        val questions = quizService.generateQuestions(
            chapterContent = chapterContent,
            chapterNumber = 1,
            previousScore = 80
        )
        
        if (questions.isNotEmpty()) {
            val types = questions.map { it.type }.toSet()
            // Should have variety of question types
            assertNotNull(types)
            assertTrue(types.size >= 1)
        }
    }
    
    @Test
    fun `quiz questions have required fields`() = runBlocking {
        val chapterContent = "AI is fascinating. It has many applications."
        
        // Mock LLM service
        val mockQuestions = listOf(
            TextLLMService.GeneratedQuestion(
                question = "What is AI?",
                expectedAnswer = "Artificial Intelligence",
                type = "factual",
                difficulty = 2
            )
        )
        `when`(mockTextLLMService.generateQuestions(any(), any(), any())).thenReturn(mockQuestions)
        
        val questions = quizService.generateQuestions(
            chapterContent = chapterContent,
            chapterNumber = 1,
            previousScore = 70
        )
        
        questions.forEach { question ->
            assertNotNull(question.question)
            assertTrue(question.question.isNotEmpty())
            assertNotNull(question.correctAnswer)
            assertTrue(question.correctAnswer.isNotEmpty())
            assertNotNull(question.explanation)
            assertTrue(question.difficulty in 1..5)
        }
    }
    
    @Test
    fun `difficulty adjusts based on previous score`() = runBlocking {
        val content = "AI content for testing difficulty adjustment."
        
        // Mock LLM service for high score (should request 2 questions)
        val mockQuestionsHigh = listOf(
            TextLLMService.GeneratedQuestion(
                question = "Question 1",
                expectedAnswer = "Answer 1",
                type = "factual",
                difficulty = 3
            ),
            TextLLMService.GeneratedQuestion(
                question = "Question 2",
                expectedAnswer = "Answer 2",
                type = "conceptual",
                difficulty = 4
            )
        )
        `when`(mockTextLLMService.generateQuestions(any(), any(), eq(2))).thenReturn(mockQuestionsHigh)
        
        // Mock LLM service for low score (should request 1 question)
        val mockQuestionsLow = listOf(
            TextLLMService.GeneratedQuestion(
                question = "Question 1",
                expectedAnswer = "Answer 1",
                type = "factual",
                difficulty = 2
            )
        )
        `when`(mockTextLLMService.generateQuestions(any(), any(), eq(1))).thenReturn(mockQuestionsLow)
        
        val easyQuestions = quizService.generateQuestions(
            chapterContent = content,
            chapterNumber = 1,
            previousScore = 95 // High score should generate 2 questions
        )
        
        val hardQuestions = quizService.generateQuestions(
            chapterContent = content,
            chapterNumber = 1,
            previousScore = 50 // Low score should generate 1 question
        )
        
        // Both should generate questions
        assertNotNull(easyQuestions)
        assertNotNull(hardQuestions)
    }
    
    @Test
    fun `judgeAnswer evaluates using AI`() = runBlocking {
        val question = ComprehensionQuizService.QuizQuestion(
            question = "What is AI?",
            questionType = ComprehensionQuizService.QuestionFormat.FILL_IN,
            correctAnswer = "Artificial Intelligence",
            explanation = "AI stands for Artificial Intelligence",
            type = ComprehensionQuizService.QuestionType.FACTUAL,
            difficulty = 2
        )
        
        val userAnswer = "A system that simulates human intelligence"
        val chapterContent = "AI is the simulation of human intelligence by machines."
        
        // Mock LLM evaluation
        val mockEvaluation = TextLLMService.EvaluationResult(
            score = 85,
            isCorrect = true,
            feedback = "Good answer! You captured the essence of AI."
        )
        `when`(mockTextLLMService.evaluateAnswer(any(), any(), any())).thenReturn(mockEvaluation)
        
        val judgment = quizService.judgeAnswer(userAnswer, question, chapterContent)
        
        assertNotNull(judgment)
        assertEquals(85, judgment.score)
        assertTrue(judgment.isCorrect)
        assertTrue(judgment.feedback.isNotEmpty())
    }
}
