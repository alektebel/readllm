package com.readllm.app.repository

import com.readllm.app.database.BookDao
import com.readllm.app.model.Book
import com.readllm.app.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {
    
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    
    suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)
    }
    
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>> {
        return bookDao.getBooksByStatus(status)
    }
    
    fun getFavoriteBooks(): Flow<List<Book>> {
        return bookDao.getFavoriteBooks()
    }
    
    suspend fun addBook(book: Book): Long {
        return bookDao.insertBook(book)
    }
    
    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }
    
    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }
    
    suspend fun updateReadingProgress(bookId: Long, position: Int) {
        bookDao.updateReadingProgress(bookId, position)
    }
    
    suspend fun updateReadingStatus(bookId: Long, status: ReadingStatus) {
        bookDao.updateReadingStatus(bookId, status)
    }
    
    suspend fun updateFavorite(bookId: Long, favorite: Boolean) {
        bookDao.updateFavorite(bookId, favorite)
    }
    
    suspend fun updateRating(bookId: Long, rating: Int) {
        bookDao.updateRating(bookId, rating)
    }
    
    fun getRecentBooks(daysAgo: Int = 7): Flow<List<Book>> {
        val timestamp = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)
        return bookDao.getRecentBooks(timestamp)
    }
    
    fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query)
    }
}
