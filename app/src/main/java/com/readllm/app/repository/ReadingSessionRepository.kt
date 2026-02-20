package com.readllm.app.repository

import com.readllm.app.database.ReadingSessionDao
import com.readllm.app.model.ReadingSession
import kotlinx.coroutines.flow.Flow

class ReadingSessionRepository(private val sessionDao: ReadingSessionDao) {
    
    fun getSessionsByBook(bookId: Long): Flow<List<ReadingSession>> {
        return sessionDao.getSessionsByBook(bookId)
    }
    
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<ReadingSession>> {
        return sessionDao.getSessionsByDateRange(startDate, endDate)
    }
    
    suspend fun startSession(bookId: Long, startPosition: Int): Long {
        val session = ReadingSession(
            bookId = bookId,
            startPosition = startPosition,
            startTime = System.currentTimeMillis()
        )
        return sessionDao.insertSession(session)
    }
    
    suspend fun endSession(session: ReadingSession, endPosition: Int, pagesRead: Int) {
        val endTime = System.currentTimeMillis()
        val duration = endTime - session.startTime
        
        val updatedSession = session.copy(
            endTime = endTime,
            duration = duration,
            endPosition = endPosition,
            pagesRead = pagesRead
        )
        
        sessionDao.updateSession(updatedSession)
    }
    
    suspend fun getTotalReadingTime(bookId: Long): Long {
        return sessionDao.getTotalReadingTime(bookId) ?: 0
    }
    
    suspend fun getTotalPagesRead(bookId: Long): Int {
        return sessionDao.getTotalPagesRead(bookId) ?: 0
    }
}
