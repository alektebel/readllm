package com.readllm.app.repository

import com.readllm.app.database.BookmarkDao
import com.readllm.app.model.Bookmark
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    
    fun getBookmarksByBook(bookId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByBook(bookId)
    }
    
    suspend fun getBookmarkById(bookmarkId: Long): Bookmark? {
        return bookmarkDao.getBookmarkById(bookmarkId)
    }
    
    suspend fun addBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark)
    }
    
    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }
    
    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }
    
    suspend fun deleteAllBookmarksForBook(bookId: Long) {
        bookmarkDao.deleteBookmarksByBook(bookId)
    }
}
