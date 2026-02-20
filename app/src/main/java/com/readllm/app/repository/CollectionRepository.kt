package com.readllm.app.repository

import com.readllm.app.database.CollectionDao
import com.readllm.app.model.Book
import com.readllm.app.model.BookCollectionCrossRef
import com.readllm.app.model.Collection as BookCollection
import kotlinx.coroutines.flow.Flow

class CollectionRepository(private val collectionDao: CollectionDao) {
    
    fun getAllCollections(): Flow<List<BookCollection>> {
        return collectionDao.getAllCollections()
    }
    
    suspend fun getCollectionById(collectionId: Long): BookCollection? {
        return collectionDao.getCollectionById(collectionId)
    }
    
    suspend fun createCollection(collection: BookCollection): Long {
        return collectionDao.insertCollection(collection)
    }
    
    suspend fun updateCollection(collection: BookCollection) {
        collectionDao.updateCollection(collection)
    }
    
    suspend fun deleteCollection(collection: BookCollection) {
        collectionDao.deleteCollection(collection)
    }
    
    suspend fun addBookToCollection(bookId: Long, collectionId: Long) {
        collectionDao.insertBookCollection(BookCollectionCrossRef(bookId, collectionId))
    }
    
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long) {
        collectionDao.deleteBookCollection(BookCollectionCrossRef(bookId, collectionId))
    }
    
    fun getBooksInCollection(collectionId: Long): Flow<List<Book>> {
        return collectionDao.getBooksInCollection(collectionId)
    }
    
    fun getCollectionsForBook(bookId: Long): Flow<List<BookCollection>> {
        return collectionDao.getCollectionsForBook(bookId)
    }
}
