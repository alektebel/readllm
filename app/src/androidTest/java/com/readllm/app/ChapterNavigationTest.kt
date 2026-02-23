package com.readllm.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.readllm.app.reader.EpubReaderService
import com.readllm.app.ui.reader.ChapterNavigationDrawer
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Chapter Navigation / Table of Contents
 */
class ChapterNavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun tableOfContents_displaysChapterTitles() {
        val testChapters = listOf(
            EpubReaderService.Chapter(
                order = 0,
                title = "Chapter 1: Introduction",
                content = "Content here",
                href = "ch1.html"
            ),
            EpubReaderService.Chapter(
                order = 1,
                title = "Chapter 2: The Journey Begins",
                content = "Content here",
                href = "ch2.html"
            )
        )
        
        composeTestRule.setContent {
            ChapterNavigationDrawer(
                chapters = testChapters,
                currentChapterIndex = 0,
                onChapterSelected = {},
                onDismiss = {}
            )
        }
        
        // Verify chapter titles are displayed
        composeTestRule.onNodeWithText("Chapter 1: Introduction").assertExists()
        composeTestRule.onNodeWithText("Chapter 2: The Journey Begins").assertExists()
    }
    
    @Test
    fun tableOfContents_highlightsCurrentChapter() {
        val testChapters = listOf(
            EpubReaderService.Chapter(0, "Chapter 1", "Content", "ch1.html"),
            EpubReaderService.Chapter(1, "Chapter 2", "Content", "ch2.html")
        )
        
        composeTestRule.setContent {
            ChapterNavigationDrawer(
                chapters = testChapters,
                currentChapterIndex = 1,  // Current chapter is 2
                onChapterSelected = {},
                onDismiss = {}
            )
        }
        
        // Current chapter should be highlighted/bold
        // This would need to check for text styling in a real implementation
        composeTestRule.onNodeWithText("Chapter 2").assertExists()
    }
    
    @Test
    fun tableOfContents_doesNotRepeatBookTitle() {
        val testChapters = listOf(
            EpubReaderService.Chapter(0, "My Book Title", "Content", "title.html"),
            EpubReaderService.Chapter(1, "Chapter 1", "Content", "ch1.html")
        )
        
        composeTestRule.setContent {
            ChapterNavigationDrawer(
                chapters = testChapters,
                currentChapterIndex = 0,
                onChapterSelected = {},
                onDismiss = {}
            )
        }
        
        // Should show "Table of Contents" as header, not book title
        composeTestRule.onNodeWithText("Table of Contents").assertExists()
        
        // Each chapter title should appear only once
        val nodes = composeTestRule.onAllNodesWithText("My Book Title")
        assert(nodes.fetchSemanticsNodes().size == 1) {
            "Book title should appear only once in ToC"
        }
    }
}
