package com.readllm.app.database

import android.content.Context
import androidx.room.*
import com.readllm.app.model.Book
import com.readllm.app.model.Bookmark
import com.readllm.app.model.BookCollectionCrossRef
import com.readllm.app.model.BookFormat
import com.readllm.app.model.ChapterScoreEntity
import com.readllm.app.model.FontFamily
import com.readllm.app.model.Highlight
import com.readllm.app.model.HighlightColor
import com.readllm.app.model.PageTurnAnimation
import com.readllm.app.model.QuizQuestionEntity
import com.readllm.app.model.ReadingSession
import com.readllm.app.model.ReadingSettings
import com.readllm.app.model.ReadingStatus
import com.readllm.app.model.ReadingTheme
import com.readllm.app.model.TextAlignment
import kotlinx.coroutines.flow.Flow
import com.readllm.app.model.Collection as BookCollection

@Dao
interface BookDao {
    
    @Query("SELECT * FROM books ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Long): Book?
    
    @Query("SELECT * FROM books WHERE readingStatus = :status ORDER BY lastReadTime DESC")
    fun getBooksByStatus(status: ReadingStatus): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY lastReadTime DESC")
    fun getFavoriteBooks(): Flow<List<Book>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long
    
    @Update
    suspend fun updateBook(book: Book)
    
    @Delete
    suspend fun deleteBook(book: Book)
    
    @Query("UPDATE books SET currentPosition = :position, lastReadTime = :timestamp WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: Long, position: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE books SET readingStatus = :status WHERE id = :bookId")
    suspend fun updateReadingStatus(bookId: Long, status: ReadingStatus)
    
    @Query("UPDATE books SET isFavorite = :favorite WHERE id = :bookId")
    suspend fun updateFavorite(bookId: Long, favorite: Boolean)
    
    @Query("UPDATE books SET rating = :rating WHERE id = :bookId")
    suspend fun updateRating(bookId: Long, rating: Int)
    
    @Query("SELECT * FROM books WHERE lastReadTime > :timestamp ORDER BY lastReadTime DESC")
    fun getRecentBooks(timestamp: Long): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>
}

@Dao
interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY chapterIndex, position")
    fun getBookmarksByBook(bookId: Long): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    suspend fun getBookmarkById(bookmarkId: Long): Bookmark?
    
    @Insert
    suspend fun insertBookmark(bookmark: Bookmark): Long
    
    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
    
    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteBookmarksByBook(bookId: Long)
}

@Dao
interface HighlightDao {
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId ORDER BY chapterIndex, startPosition")
    fun getHighlightsByBook(bookId: Long): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND chapterIndex = :chapterIndex ORDER BY startPosition")
    fun getHighlightsByChapter(bookId: Long, chapterIndex: Int): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights WHERE id = :highlightId")
    suspend fun getHighlightById(highlightId: Long): Highlight?
    
    @Insert
    suspend fun insertHighlight(highlight: Highlight): Long
    
    @Update
    suspend fun updateHighlight(highlight: Highlight)
    
    @Delete
    suspend fun deleteHighlight(highlight: Highlight)
    
    @Query("DELETE FROM highlights WHERE bookId = :bookId")
    suspend fun deleteHighlightsByBook(bookId: Long)
}

@Dao
interface CollectionDao {
    
    @Query("SELECT * FROM collections ORDER BY name")
    fun getAllCollections(): Flow<List<BookCollection>>
    
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): BookCollection?
    
    @Insert
    suspend fun insertCollection(collection: BookCollection): Long
    
    @Update
    suspend fun updateCollection(collection: BookCollection)
    
    @Delete
    suspend fun deleteCollection(collection: BookCollection)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookCollection(crossRef: BookCollectionCrossRef)
    
    @Delete
    suspend fun deleteBookCollection(crossRef: BookCollectionCrossRef)
    
    @Query("SELECT books.* FROM books INNER JOIN book_collection_cross_ref ON books.id = book_collection_cross_ref.bookId WHERE book_collection_cross_ref.collectionId = :collectionId")
    fun getBooksInCollection(collectionId: Long): Flow<List<Book>>
    
    @Query("SELECT collections.* FROM collections INNER JOIN book_collection_cross_ref ON collections.id = book_collection_cross_ref.collectionId WHERE book_collection_cross_ref.bookId = :bookId")
    fun getCollectionsForBook(bookId: Long): Flow<List<BookCollection>>
}

@Dao
interface ReadingSettingsDao {
    
    @Query("SELECT * FROM reading_settings WHERE id = 1")
    suspend fun getSettings(): ReadingSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ReadingSettings)
    
    @Update
    suspend fun updateSettings(settings: ReadingSettings)
}

@Dao
interface ReadingSessionDao {
    
    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY startTime DESC")
    fun getSessionsByBook(bookId: Long): Flow<List<ReadingSession>>
    
    @Query("SELECT * FROM reading_sessions WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<ReadingSession>>
    
    @Insert
    suspend fun insertSession(session: ReadingSession): Long
    
    @Update
    suspend fun updateSession(session: ReadingSession)
    
    @Delete
    suspend fun deleteSession(session: ReadingSession)
    
    @Query("SELECT SUM(duration) FROM reading_sessions WHERE bookId = :bookId")
    suspend fun getTotalReadingTime(bookId: Long): Long?
    
    @Query("SELECT SUM(pagesRead) FROM reading_sessions WHERE bookId = :bookId")
    suspend fun getTotalPagesRead(bookId: Long): Int?
}

@Dao
interface ChapterScoreDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ChapterScoreEntity): Long
    
    @Query("SELECT * FROM chapter_scores WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getScoresByBook(bookId: Long): Flow<List<ChapterScoreEntity>>
    
    @Query("SELECT * FROM chapter_scores WHERE bookId = :bookId AND chapterId = :chapterId ORDER BY timestamp DESC")
    suspend fun getScoresForChapter(bookId: Long, chapterId: Int): List<ChapterScoreEntity>
    
    @Query("SELECT * FROM chapter_scores WHERE bookId = :bookId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentScores(bookId: Long, limit: Int = 5): List<ChapterScoreEntity>
    
    @Query("SELECT AVG(scorePercentage) FROM chapter_scores WHERE bookId = :bookId")
    suspend fun getAverageScore(bookId: Long): Float?
    
    @Query("DELETE FROM chapter_scores WHERE bookId = :bookId")
    suspend fun deleteScoresByBook(bookId: Long)
}

@Dao
interface QuizQuestionDao {
    
    @Insert
    suspend fun insertQuestion(question: QuizQuestionEntity): Long
    
    @Insert
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)
    
    @Query("SELECT * FROM quiz_questions WHERE scoreId = :scoreId")
    suspend fun getQuestionsByScore(scoreId: Long): List<QuizQuestionEntity>
    
    @Query("SELECT * FROM quiz_questions WHERE questionType = :type AND isCorrect = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getIncorrectQuestionsByType(type: String, limit: Int = 10): List<QuizQuestionEntity>
    
    @Query("DELETE FROM quiz_questions WHERE scoreId = :scoreId")
    suspend fun deleteQuestionsByScore(scoreId: Long)
}

@Database(
    entities = [
        Book::class,
        Bookmark::class,
        Highlight::class,
        BookCollection::class,
        BookCollectionCrossRef::class,
        ReadingSettings::class,
        ReadingSession::class,
        ChapterScoreEntity::class,
        QuizQuestionEntity::class
    ],
    version = 2, // Incremented version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun collectionDao(): CollectionDao
    abstract fun readingSettingsDao(): ReadingSettingsDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun chapterScoreDao(): ChapterScoreDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readllm_database"
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Type converters for enums
class Converters {
    @TypeConverter
    fun fromReadingStatus(value: ReadingStatus): String = value.name
    
    @TypeConverter
    fun toReadingStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)
    
    @TypeConverter
    fun fromBookFormat(value: BookFormat): String = value.name
    
    @TypeConverter
    fun toBookFormat(value: String): BookFormat = BookFormat.valueOf(value)
    
    @TypeConverter
    fun fromHighlightColor(value: HighlightColor): String = value.name
    
    @TypeConverter
    fun toHighlightColor(value: String): HighlightColor = HighlightColor.valueOf(value)
    
    @TypeConverter
    fun fromReadingTheme(value: ReadingTheme): String = value.name
    
    @TypeConverter
    fun toReadingTheme(value: String): ReadingTheme = ReadingTheme.valueOf(value)
    
    @TypeConverter
    fun fromFontFamily(value: FontFamily): String = value.name
    
    @TypeConverter
    fun toFontFamily(value: String): FontFamily = FontFamily.valueOf(value)
    
    @TypeConverter
    fun fromPageTurnAnimation(value: PageTurnAnimation): String = value.name
    
    @TypeConverter
    fun toPageTurnAnimation(value: String): PageTurnAnimation = PageTurnAnimation.valueOf(value)
    
    @TypeConverter
    fun fromTextAlignment(value: TextAlignment): String = value.name
    
    @TypeConverter
    fun toTextAlignment(value: String): TextAlignment = TextAlignment.valueOf(value)
}
