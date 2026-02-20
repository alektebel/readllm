package com.readllm.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HighlightColor {
    YELLOW,
    GREEN,
    BLUE,
    PINK,
    PURPLE,
    ORANGE
}

@Entity(
    tableName = "highlights",
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
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val startPosition: Int,
    val endPosition: Int,
    val selectedText: String,
    val color: HighlightColor = HighlightColor.YELLOW,
    val note: String? = null,
    val createdTime: Long = System.currentTimeMillis()
)
