package com.readllm.app.repository

import com.readllm.app.database.HighlightDao
import com.readllm.app.model.Highlight
import kotlinx.coroutines.flow.Flow

class HighlightRepository(private val highlightDao: HighlightDao) {
    
    fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>> {
        return highlightDao.getHighlightsByBook(bookId)
    }
    
    fun getHighlightsByChapter(bookId: Long, chapterIndex: Int): Flow<List<Highlight>> {
        return highlightDao.getHighlightsByChapter(bookId, chapterIndex)
    }
    
    suspend fun getHighlightById(highlightId: Long): Highlight? {
        return highlightDao.getHighlightById(highlightId)
    }
    
    suspend fun addHighlight(highlight: Highlight): Long {
        return highlightDao.insertHighlight(highlight)
    }
    
    suspend fun updateHighlight(highlight: Highlight) {
        highlightDao.updateHighlight(highlight)
    }
    
    suspend fun deleteHighlight(highlight: Highlight) {
        highlightDao.deleteHighlight(highlight)
    }
    
    suspend fun deleteAllHighlightsForBook(bookId: Long) {
        highlightDao.deleteHighlightsByBook(bookId)
    }
}
