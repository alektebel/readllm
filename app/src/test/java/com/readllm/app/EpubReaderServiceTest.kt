package com.readllm.app.reader

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for EpubReaderService
 */
class EpubReaderServiceTest {
    
    private lateinit var epubReader: EpubReaderService
    
    @Before
    fun setup() {
        epubReader = EpubReaderService()
    }
    
    @Test
    fun `service initializes successfully`() {
        assertNotNull(epubReader)
    }
    
    @Test
    fun `getChapterCount returns 0 for book with no chapters`() {
        val emptyBook = EpubReaderService.EpubBook(
            title = "Test",
            author = "Author",
            chapters = emptyList(),
            images = emptyMap()
        )
        
        assertEquals(0, epubReader.getChapterCount(emptyBook))
    }
    
    @Test
    fun `getChapterCount returns correct count`() {
        val book = EpubReaderService.EpubBook(
            title = "Test",
            author = "Author",
            chapters = listOf(
                EpubReaderService.Chapter("Ch1", "Content", 0),
                EpubReaderService.Chapter("Ch2", "Content", 1)
            ),
            images = emptyMap()
        )
        
        assertEquals(2, epubReader.getChapterCount(book))
    }
    
    @Test
    fun `getChapterContent returns empty for invalid index`() {
        val book = EpubReaderService.EpubBook(
            title = "Test",
            author = "Author",
            chapters = listOf(
                EpubReaderService.Chapter("Ch1", "Content", 0)
            ),
            images = emptyMap()
        )
        
        val content = epubReader.getChapterContent(book, 10)
        assertEquals("", content.text)
        assertTrue(content.images.isEmpty())
    }
    
    @Test
    fun `getBookMetadata returns correct metadata`() {
        val book = EpubReaderService.EpubBook(
            title = "Test Book",
            author = "Test Author",
            chapters = emptyList(),
            images = emptyMap()
        )
        
        val metadata = epubReader.getBookMetadata(book)
        assertEquals("Test Book", metadata.title)
        assertEquals("Test Author", metadata.author)
    }
}
