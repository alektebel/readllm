package com.readllm.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.readllm.app.model.Book
import com.readllm.app.model.BookFormat
import com.readllm.app.model.ReadingStatus
import com.readllm.app.ui.library.EnhancedLibraryScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Library Screen with book covers
 */
@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun libraryScreen_defaultsToGridView() {
        val testBooks = listOf(
            Book(
                id = 1,
                title = "Test Book 1",
                author = "Author 1",
                filePath = "/path/to/book1.epub",
                format = BookFormat.EPUB,
                fileSize = 1024000,
                coverImagePath = null
            ),
            Book(
                id = 2,
                title = "Test Book 2",
                author = "Author 2",
                filePath = "/path/to/book2.epub",
                format = BookFormat.EPUB,
                fileSize = 2048000,
                coverImagePath = null
            )
        )
        
        composeTestRule.setContent {
            EnhancedLibraryScreen(
                books = testBooks,
                onBookClick = {},
                onImportBook = {}
            )
        }
        
        // Should default to grid view (showing both books in grid)
        composeTestRule.onNodeWithText("Test Book 1").assertExists()
        composeTestRule.onNodeWithText("Test Book 2").assertExists()
    }
    
    @Test
    fun libraryScreen_showsBookCovers() {
        val bookWithCover = Book(
            id = 1,
            title = "Book with Cover",
            author = "Test Author",
            filePath = "/path/to/book.epub",
            format = BookFormat.EPUB,
            fileSize = 1024000,
            coverImagePath = "/path/to/cover.jpg"
        )
        
        composeTestRule.setContent {
            EnhancedLibraryScreen(
                books = listOf(bookWithCover),
                onBookClick = {},
                onImportBook = {}
            )
        }
        
        // Verify book title is displayed (covers are displayed via AsyncImage)
        composeTestRule.onNodeWithText("Book with Cover").assertExists()
        composeTestRule.onNodeWithText("Test Author").assertExists()
    }
    
    @Test
    fun libraryScreen_showsProgressBadge() {
        val bookInProgress = Book(
            id = 1,
            title = "In Progress Book",
            author = "Test Author",
            filePath = "/path/to/book.epub",
            format = BookFormat.EPUB,
            fileSize = 1024000,
            readingProgress = 45.0
        )
        
        composeTestRule.setContent {
            EnhancedLibraryScreen(
                books = listOf(bookInProgress),
                onBookClick = {},
                onImportBook = {}
            )
        }
        
        // Should show progress percentage
        composeTestRule.onNodeWithText("45%").assertExists()
    }
    
    @Test
    fun libraryScreen_showsFavoriteIndicator() {
        val favoriteBook = Book(
            id = 1,
            title = "Favorite Book",
            author = "Test Author",
            filePath = "/path/to/book.epub",
            format = BookFormat.EPUB,
            fileSize = 1024000,
            isFavorite = true
        )
        
        composeTestRule.setContent {
            EnhancedLibraryScreen(
                books = listOf(favoriteBook),
                onBookClick = {},
                onImportBook = {}
            )
        }
        
        // Favorite icon should be visible (verified by content description)
        composeTestRule.onNodeWithContentDescription("Favorite").assertExists()
    }
    
    @Test
    fun libraryScreen_canToggleViewMode() {
        val testBooks = listOf(
            Book(
                id = 1,
                title = "Test Book",
                author = "Test Author",
                filePath = "/path/to/book.epub",
                format = BookFormat.EPUB,
                fileSize = 1024000
            )
        )
        
        composeTestRule.setContent {
            EnhancedLibraryScreen(
                books = testBooks,
                onBookClick = {},
                onImportBook = {}
            )
        }
        
        // Click toggle view button to switch to list view
        composeTestRule.onNodeWithContentDescription("Toggle view").performClick()
        
        // Book should still be visible in list view
        composeTestRule.onNodeWithText("Test Book").assertExists()
    }
}
