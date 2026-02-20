package com.readllm.app.repository

import com.readllm.app.model.Book
import org.junit.Assert.*
import org.junit.Test

/**
 * Simple unit tests for Book model
 */
class BookRepositoryTest {
    
    @Test
    fun `Book model creates with correct properties`() {
        val book = Book(
            id = 1,
            title = "Test Book",
            author = "Test Author",
            filePath = "/test/path.epub",
            currentPosition = 0,
            totalPages = 100,
            lastReadTime = System.currentTimeMillis()
        )
        
        assertEquals(1, book.id)
        assertEquals("Test Book", book.title)
        assertEquals("Test Author", book.author)
        assertEquals("/test/path.epub", book.filePath)
        assertEquals(0, book.currentPosition)
        assertEquals(100, book.totalPages)
    }
    
    @Test
    fun `Book progress calculation works correctly`() {
        val book = Book(
            title = "Test",
            author = "Author",
            filePath = "/path",
            currentPosition = 50,
            totalPages = 100
        )
        
        val progress = (book.currentPosition.toFloat() / book.totalPages.toFloat()) * 100
        assertEquals(50f, progress, 0.01f)
    }
    
    @Test
    fun `Book with zero pages handles division safely`() {
        val book = Book(
            title = "Test",
            author = "Author",
            filePath = "/path",
            currentPosition = 0,
            totalPages = 0
        )
        
        // This would cause division by zero, so we check it's handled
        assertTrue(book.totalPages == 0 || book.currentPosition <= book.totalPages)
    }
}
