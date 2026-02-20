package com.readllm.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReadingStatus {
    UNREAD,
    READING,
    FINISHED
}

enum class BookFormat {
    EPUB,
    PDF,
    TXT,
    MOBI,
    AZW3,
    FB2,
    OTHER
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val filePath: String,
    val format: BookFormat = BookFormat.EPUB,
    val coverImagePath: String? = null,
    val fileSize: Long = 0, // in bytes
    val currentPosition: Int = 0,
    val totalPages: Int = 0,
    val currentChapter: Int = 0,
    val totalChapters: Int = 0,
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    val readingProgress: Float = 0f, // 0-100%
    val timeSpentReading: Long = 0, // in milliseconds
    val lastReadTime: Long = System.currentTimeMillis(),
    val addedTime: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val rating: Int = 0, // 0-5 stars
    val language: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val isbn: String? = null,
    val publicationYear: Int? = null
)
