package com.readllm.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks reading sessions for statistics
 */
@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val duration: Long = 0, // in milliseconds
    val pagesRead: Int = 0,
    val startPosition: Int = 0,
    val endPosition: Int = 0
)
