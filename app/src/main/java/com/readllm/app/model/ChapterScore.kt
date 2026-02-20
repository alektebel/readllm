package com.readllm.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.readllm.app.quiz.ComprehensionQuizService

/**
 * Database entity for storing chapter comprehension quiz scores
 */
@Entity(tableName = "chapter_scores")
data class ChapterScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val bookId: Long,
    val chapterId: Int,
    val chapterTitle: String,
    
    val questionsAsked: Int,
    val correctAnswers: Int,
    val scorePercentage: Float,
    
    val timeSpent: Long, // milliseconds
    val timestamp: Long = System.currentTimeMillis(),
    
    // Question type breakdown (stored as JSON string)
    val factualScore: Float? = null,
    val conceptualScore: Float? = null,
    val inferenceScore: Float? = null,
    val visualScore: Float? = null,
    val summaryScore: Float? = null
)

/**
 * Database entity for storing individual quiz questions and answers
 */
@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val scoreId: Long, // Foreign key to chapter_scores
    val questionId: String,
    val questionText: String,
    val questionType: String,
    
    val userAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for comprehension analytics
 */
data class ComprehensionAnalytics(
    val bookId: Long,
    val bookTitle: String,
    
    val totalChaptersRead: Int,
    val totalQuestionsAnswered: Int,
    val totalCorrectAnswers: Int,
    
    val overallScorePercentage: Float,
    val averageTimePerChapter: Long,
    
    val strongestQuestionType: ComprehensionQuizService.QuestionType?,
    val weakestQuestionType: ComprehensionQuizService.QuestionType?,
    
    val improvementTrend: Float, // Positive = improving, negative = declining
    val chapterScores: List<ChapterScoreEntity>
)
