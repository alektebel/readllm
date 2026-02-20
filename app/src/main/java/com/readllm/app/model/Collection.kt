package com.readllm.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdTime: Long = System.currentTimeMillis(),
    val color: Int? = null // Optional color for the collection
)

@Entity(
    tableName = "book_collection_cross_ref",
    primaryKeys = ["bookId", "collectionId"]
)
data class BookCollectionCrossRef(
    val bookId: Long,
    val collectionId: Long
)
