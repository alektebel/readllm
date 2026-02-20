package com.readllm.app.repository

import com.readllm.app.database.ChapterScoreDao
import com.readllm.app.database.QuizQuestionDao
import com.readllm.app.model.ChapterScoreEntity
import com.readllm.app.model.ComprehensionAnalytics
import com.readllm.app.model.QuizQuestionEntity
import com.readllm.app.quiz.ComprehensionQuizService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class QuizRepository(
    private val chapterScoreDao: ChapterScoreDao,
    private val quizQuestionDao: QuizQuestionDao
) {
    
    /**
     * Save quiz results
     */
    suspend fun saveQuizResults(
        score: ChapterScoreEntity,
        questions: List<QuizQuestionEntity>
    ): Long {
        val scoreId = chapterScoreDao.insertScore(score)
        
        // Update questions with the score ID
        val questionsWithScoreId = questions.map { it.copy(scoreId = scoreId) }
        quizQuestionDao.insertQuestions(questionsWithScoreId)
        
        return scoreId
    }
    
    /**
     * Simple method to save chapter score
     */
    suspend fun saveChapterScore(
        bookId: Long,
        chapterNumber: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        averageDifficulty: Double
    ): Long {
        val score = ChapterScoreEntity(
            bookId = bookId,
            chapterId = chapterNumber,
            chapterTitle = "Chapter $chapterNumber",
            questionsAsked = totalQuestions,
            correctAnswers = correctAnswers,
            scorePercentage = (correctAnswers.toFloat() / totalQuestions * 100),
            timeSpent = 0 // TODO: Track time
        )
        return chapterScoreDao.insertScore(score)
    }
    
    /**
     * Get all scores for a book
     */
    fun getBookScores(bookId: Long): Flow<List<ChapterScoreEntity>> {
        return chapterScoreDao.getScoresByBook(bookId)
    }
    
    /**
     * Get chapter scores as Flow
     */
    fun getChapterScores(bookId: Long): Flow<List<ChapterScoreEntity>> {
        return chapterScoreDao.getScoresByBook(bookId)
    }
    
    /**
     * Get scores for a specific chapter
     */
    suspend fun getChapterScoresForChapter(bookId: Long, chapterId: Int): List<ChapterScoreEntity> {
        return chapterScoreDao.getScoresForChapter(bookId, chapterId)
    }
    
    /**
     * Get recent quiz performance
     */
    suspend fun getRecentPerformance(bookId: Long, limit: Int = 5): List<ChapterScoreEntity> {
        return chapterScoreDao.getRecentScores(bookId, limit)
    }
    
    /**
     * Calculate comprehensive analytics for a book
     */
    suspend fun getComprehensionAnalytics(
        bookId: Long,
        bookTitle: String
    ): ComprehensionAnalytics {
        val allScores = chapterScoreDao.getScoresByBook(bookId).first()
        
        if (allScores.isEmpty()) {
            return ComprehensionAnalytics(
                bookId = bookId,
                bookTitle = bookTitle,
                totalChaptersRead = 0,
                totalQuestionsAnswered = 0,
                totalCorrectAnswers = 0,
                overallScorePercentage = 0f,
                averageTimePerChapter = 0,
                strongestQuestionType = null,
                weakestQuestionType = null,
                improvementTrend = 0f,
                chapterScores = emptyList()
            )
        }
        
        val totalQuestions = allScores.sumOf { it.questionsAsked }
        val totalCorrect = allScores.sumOf { it.correctAnswers }
        val overallScore = (totalCorrect.toFloat() / totalQuestions) * 100
        val avgTime = allScores.map { it.timeSpent }.average().toLong()
        
        // Find strongest and weakest question types - MVP: Simplified
        // TODO: Implement detailed type tracking
        val strongest: ComprehensionQuizService.QuestionType? = null
        val weakest: ComprehensionQuizService.QuestionType? = null
        
        // Calculate improvement trend (compare recent vs older scores)
        val improvementTrend = if (allScores.size >= 3) {
            val recentAvg = allScores.take(3).map { it.scorePercentage }.average().toFloat()
            val olderAvg = allScores.drop(3).take(3).map { it.scorePercentage }.average().toFloat()
            recentAvg - olderAvg
        } else {
            0f
        }
        
        return ComprehensionAnalytics(
            bookId = bookId,
            bookTitle = bookTitle,
            totalChaptersRead = allScores.distinctBy { it.chapterId }.size,
            totalQuestionsAnswered = totalQuestions,
            totalCorrectAnswers = totalCorrect,
            overallScorePercentage = overallScore,
            averageTimePerChapter = avgTime,
            strongestQuestionType = strongest,
            weakestQuestionType = weakest,
            improvementTrend = improvementTrend,
            chapterScores = allScores
        )
    }
    
    /**
     * Get questions user got wrong for review
     */
    suspend fun getIncorrectQuestions(
        type: ComprehensionQuizService.QuestionType,
        limit: Int = 10
    ): List<QuizQuestionEntity> {
        return quizQuestionDao.getIncorrectQuestionsByType(type.name, limit)
    }
    
    /**
     * Delete all quiz data for a book
     */
    suspend fun deleteBookQuizData(bookId: Long) {
        chapterScoreDao.deleteScoresByBook(bookId)
    }
}
