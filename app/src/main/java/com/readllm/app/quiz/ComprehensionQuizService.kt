package com.readllm.app.quiz

import com.readllm.app.llm.TextLLMService

/**
 * Interactive Reading Comprehension Quiz Service
 * 
 * Generates contextual questions at chapter endings using AI
 * and tracks user comprehension scores per chapter.
 */
class ComprehensionQuizService(private val textLLMService: TextLLMService) {
    
    // Simplified data class for quiz questions
    data class QuizQuestion(
        val question: String,
        val questionType: QuestionFormat = QuestionFormat.FILL_IN,
        val correctAnswer: String,
        val explanation: String,
        val type: QuestionType,
        val difficulty: Int, // 1-5
        val acceptableAnswers: List<String> = listOf() // Alternative acceptable answers
    )
    
    enum class QuestionFormat {
        FILL_IN,  // User types their answer
        MULTIPLE_CHOICE  // Traditional multiple choice (legacy)
    }
    
    enum class QuestionType {
        FACTUAL,
        CONCEPTUAL,
        INFERENCE,
        VISUAL_CONTENT
    }
    
    /**
     * Check if quiz should be shown
     */
    fun shouldShowQuiz(chapterContent: String, chapterNumber: Int): Boolean {
        // Show quiz at every chapter end for MVP
        return chapterContent.isNotBlank()
    }
    
    /**
     * Generate comprehension questions for a chapter using AI
     * Reduced to 1-2 questions for better user experience
     */
    suspend fun generateQuestions(
        chapterContent: String,
        chapterNumber: Int,
        previousScore: Int
    ): List<QuizQuestion> {
        // Determine number of questions based on previous performance
        val numQuestions = if (previousScore > 70) 2 else 1
        
        // Use AI to generate questions based on actual chapter content
        val generatedQuestions = textLLMService.generateQuestions(
            chapterContent = chapterContent,
            chapterNumber = chapterNumber,
            numQuestions = numQuestions
        )
        
        // Convert AI-generated questions to QuizQuestion format
        return generatedQuestions.map { generated ->
            QuizQuestion(
                question = generated.question,
                questionType = QuestionFormat.FILL_IN,
                correctAnswer = generated.expectedAnswer,
                acceptableAnswers = listOf(), // AI will evaluate semantically
                explanation = "Based on the chapter content",
                type = when (generated.type.lowercase()) {
                    "factual" -> QuestionType.FACTUAL
                    "conceptual" -> QuestionType.CONCEPTUAL
                    "inference" -> QuestionType.INFERENCE
                    else -> QuestionType.FACTUAL
                },
                difficulty = generated.difficulty
            )
        }
    }
    
    /**
     * Judge user response using AI for semantic understanding
     * Falls back to keyword matching if AI is unavailable
     */
    suspend fun judgeAnswer(
        userAnswer: String,
        question: QuizQuestion,
        chapterContent: String
    ): AnswerJudgment {
        if (question.questionType == QuestionFormat.MULTIPLE_CHOICE) {
            // Legacy multiple choice handling
            return AnswerJudgment(
                isCorrect = userAnswer.equals(question.correctAnswer, ignoreCase = true),
                score = if (userAnswer.equals(question.correctAnswer, ignoreCase = true)) 100 else 0,
                feedback = if (userAnswer.equals(question.correctAnswer, ignoreCase = true)) 
                    "Correct!" else "Incorrect. ${question.explanation}"
            )
        }
        
        // Use AI to evaluate the answer semantically
        val evaluation = textLLMService.evaluateAnswer(
            userAnswer = userAnswer,
            question = question.question,
            chapterContent = chapterContent
        )
        
        return AnswerJudgment(
            isCorrect = evaluation.isCorrect,
            score = evaluation.score,
            feedback = evaluation.feedback
        )
    }
    
    data class AnswerJudgment(
        val isCorrect: Boolean,
        val score: Int,  // 0-100
        val feedback: String
    )
}
