package com.readllm.app.reader

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.io.InputStream

/**
 * Integration tests for EPUB reading functionality
 * 
 * These tests validate that the app correctly reads and parses
 * actual EPUB files, ensuring text extraction is accurate.
 */
class EpubIntegrationTest {
    
    private lateinit var epubReader: EpubReaderService
    private var sampleEpubStream: InputStream? = null
    
    @Before
    fun setup() {
        epubReader = EpubReaderService()
        
        // Load the sample book from assets
        // In actual test environment, this would use context.assets
        // For now, we'll try to load from the project path
        val samplePath = "../app/src/main/assets/sample_book.epub"
        val file = File(samplePath)
        
        if (file.exists()) {
            sampleEpubStream = file.inputStream()
        }
    }
    
    @Test
    fun `sample epub loads successfully`() {
        // Skip test if sample file not available in test environment
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        assertNotNull(book)
    }
    
    @Test
    fun `sample epub has correct metadata`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        
        // Validate metadata from content.opf
        assertEquals("ReadLLM Sample Book: Introduction to AI", book.title)
        assertEquals("ReadLLM Team", book.author)
    }
    
    @Test
    fun `sample epub has correct chapter count`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        
        // Sample book has 3 content chapters + 1 nav file = 4 total HTML files
        // The parser includes all .xhtml files, including nav.xhtml
        assertTrue("Should have at least 3 chapters", epubReader.getChapterCount(book) >= 3)
    }
    
    @Test
    fun `sample epub chapters have correct titles`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        
        // Validate chapter titles
        assertTrue(book.chapters.size >= 3)
        
        val chapter1Title = book.chapters[0].title
        val chapter2Title = book.chapters[1].title
        val chapter3Title = book.chapters[2].title
        
        assertTrue("Chapter 1 title should contain 'Artificial Intelligence'", 
            chapter1Title.contains("Artificial Intelligence", ignoreCase = true))
        assertTrue("Chapter 2 title should contain 'Machine Learning'", 
            chapter2Title.contains("Machine Learning", ignoreCase = true))
        assertTrue("Chapter 3 title should contain 'Future'", 
            chapter3Title.contains("Future", ignoreCase = true))
    }
    
    @Test
    fun `chapter 1 contains expected content`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapter1Content = epubReader.getChapterContent(book, 0)
        
        // Validate key content is present
        assertTrue("Should contain John McCarthy", 
            chapter1Content.text.contains("John McCarthy"))
        assertTrue("Should contain Dartmouth Conference", 
            chapter1Content.text.contains("Dartmouth Conference"))
        assertTrue("Should contain 1956", 
            chapter1Content.text.contains("1956"))
        
        // Validate key concepts are present
        assertTrue("Should mention Learning", 
            chapter1Content.text.contains("Learning"))
        assertTrue("Should mention Reasoning", 
            chapter1Content.text.contains("Reasoning"))
        assertTrue("Should mention Perception", 
            chapter1Content.text.contains("Perception"))
        assertTrue("Should mention Natural Language Processing", 
            chapter1Content.text.contains("Natural Language Processing"))
        
        // Validate AI types are described
        assertTrue("Should describe Narrow AI", 
            chapter1Content.text.contains("Narrow AI"))
        assertTrue("Should describe General AI", 
            chapter1Content.text.contains("General AI"))
        assertTrue("Should describe Super AI", 
            chapter1Content.text.contains("Super AI"))
    }
    
    @Test
    fun `chapter 2 contains expected content`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapter2Content = epubReader.getChapterContent(book, 1)
        
        // Validate ML fundamentals are present
        assertTrue("Should describe machine learning", 
            chapter2Content.text.contains("Machine Learning"))
        
        // Validate learning types
        assertTrue("Should describe Supervised Learning", 
            chapter2Content.text.contains("Supervised Learning"))
        assertTrue("Should describe Unsupervised Learning", 
            chapter2Content.text.contains("Unsupervised Learning"))
        assertTrue("Should describe Reinforcement Learning", 
            chapter2Content.text.contains("Reinforcement Learning"))
        
        // Validate neural networks section
        assertTrue("Should mention Neural Networks", 
            chapter2Content.text.contains("Neural"))
        assertTrue("Should mention Input Layer", 
            chapter2Content.text.contains("Input Layer"))
        assertTrue("Should mention Hidden Layers", 
            chapter2Content.text.contains("Hidden Layers"))
        assertTrue("Should mention Output Layer", 
            chapter2Content.text.contains("Output Layer"))
        
        // Validate challenges are discussed
        assertTrue("Should mention Overfitting", 
            chapter2Content.text.contains("Overfitting"))
        assertTrue("Should mention Bias", 
            chapter2Content.text.contains("Bias"))
    }
    
    @Test
    fun `chapter 3 contains expected content`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapter3Content = epubReader.getChapterContent(book, 2)
        
        // Validate future trends
        assertTrue("Should discuss Multimodal AI", 
            chapter3Content.text.contains("Multimodal AI"))
        assertTrue("Should discuss Edge AI", 
            chapter3Content.text.contains("Edge AI"))
        assertTrue("Should discuss Explainable AI", 
            chapter3Content.text.contains("Explainable AI"))
        
        // Validate societal impacts
        assertTrue("Should mention Healthcare", 
            chapter3Content.text.contains("Healthcare"))
        assertTrue("Should mention Education", 
            chapter3Content.text.contains("Education"))
        assertTrue("Should mention Climate", 
            chapter3Content.text.contains("Climate"))
        
        // Validate ethical considerations
        assertTrue("Should discuss Privacy", 
            chapter3Content.text.contains("Privacy"))
        assertTrue("Should discuss Fairness", 
            chapter3Content.text.contains("Fairness"))
        assertTrue("Should discuss Accountability", 
            chapter3Content.text.contains("Accountability"))
    }
    
    @Test
    fun `html content is properly cleaned`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapter1Content = epubReader.getChapterContent(book, 0)
        
        // Ensure no XML declarations remain
        assertFalse("Should not contain XML declaration", 
            chapter1Content.text.contains("<?xml"))
        
        // Ensure no DOCTYPE remains
        assertFalse("Should not contain DOCTYPE", 
            chapter1Content.text.contains("<!DOCTYPE"))
        
        // Ensure no head tags remain
        assertFalse("Should not contain <head> tag", 
            chapter1Content.text.contains("<head>"))
        
        // Ensure HTML tags are present (not completely stripped)
        assertTrue("Should still contain HTML formatting tags like <h1>", 
            chapter1Content.text.contains("<h1>") || chapter1Content.text.contains("<p>"))
    }
    
    @Test
    fun `all chapters are accessible`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapterCount = epubReader.getChapterCount(book)
        
        // Verify all chapters can be loaded
        for (i in 0 until chapterCount) {
            val content = epubReader.getChapterContent(book, i)
            assertNotNull("Chapter $i content should not be null", content)
            assertTrue("Chapter $i should have non-empty text", content.text.isNotEmpty())
        }
    }
    
    @Test
    fun `invalid chapter index returns empty content`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        
        // Try to access chapter beyond the book's chapter count
        val invalidContent = epubReader.getChapterContent(book, 999)
        assertEquals("", invalidContent.text)
        assertTrue(invalidContent.images.isEmpty())
    }
    
    @Test
    fun `chapter content maintains paragraph structure`() {
        if (sampleEpubStream == null) {
            println("Skipping test - sample_book.epub not accessible in test environment")
            return
        }
        
        val book = epubReader.loadEpub(sampleEpubStream!!)
        val chapter1Content = epubReader.getChapterContent(book, 0)
        
        // Check that paragraph tags are preserved
        assertTrue("Should contain paragraph tags", 
            chapter1Content.text.contains("<p>"))
        
        // Check that headings are preserved
        assertTrue("Should contain heading tags", 
            chapter1Content.text.contains("<h1>") || chapter1Content.text.contains("<h2>"))
        
        // Check that lists are preserved
        assertTrue("Should contain list tags", 
            chapter1Content.text.contains("<ul>") || chapter1Content.text.contains("<li>"))
    }
}
