package com.readllm.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.readllm.app.database.AppDatabase
import com.readllm.app.llm.ModelDownloadService
import com.readllm.app.model.Book
import com.readllm.app.model.BookFormat
import com.readllm.app.model.ReadingStatus
import com.readllm.app.reader.EpubReaderService
import com.readllm.app.repository.BookRepository
import com.readllm.app.repository.BookmarkRepository
import com.readllm.app.repository.HighlightRepository
import com.readllm.app.repository.CollectionRepository
import com.readllm.app.repository.ReadingSettingsRepository
import com.readllm.app.repository.ReadingSessionRepository
import com.readllm.app.scanner.BookScanner
import com.readllm.app.ui.library.EnhancedLibraryScreen
import com.readllm.app.ui.settings.SettingsScreen
import com.readllm.app.ui.theme.ReadLLMTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    
    private lateinit var bookRepository: BookRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var highlightRepository: HighlightRepository
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var readingSettingsRepository: ReadingSettingsRepository
    private lateinit var readingSessionRepository: ReadingSessionRepository
    private lateinit var bookScanner: BookScanner
    private lateinit var modelDownloadService: ModelDownloadService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        bookRepository = BookRepository(database.bookDao())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())
        highlightRepository = HighlightRepository(database.highlightDao())
        collectionRepository = CollectionRepository(database.collectionDao())
        readingSettingsRepository = ReadingSettingsRepository(database.readingSettingsDao())
        readingSessionRepository = ReadingSessionRepository(database.readingSessionDao())
        bookScanner = BookScanner(this)
        modelDownloadService = ModelDownloadService(this)
        
        // Check if model needs to be downloaded
        lifecycleScope.launch {
            if (!modelDownloadService.isModelDownloaded()) {
                // Model will be downloaded in the background
                modelDownloadService.downloadModel().collect { progress ->
                    // Log download progress
                    android.util.Log.d("MainActivity", "Model download: ${progress.status} - ${progress.progress}%")
                }
            }
        }
        
        setContent {
            ReadLLMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraryScreenWrapper()
                }
            }
        }
    }
    
    private fun scanDeviceForBooks() {
        lifecycleScope.launch {
            try {
                val scannedBooks = bookScanner.scanForBooks()
                
                scannedBooks.forEach { scanned ->
                    // Check if book already exists in database
                    val existingBooks = bookRepository.allBooks
                    var alreadyExists = false
                    
                    existingBooks.collect { books ->
                        alreadyExists = books.any { it.filePath == scanned.filePath }
                        return@collect
                    }
                    
                    if (!alreadyExists) {
                        // Parse EPUB to get metadata
                        val epubReader = EpubReaderService()
                        val epubData = File(scanned.filePath).inputStream().use { 
                            epubReader.loadEpub(it)
                        }
                        
                        // Save cover image if available
                        var coverImagePath: String? = null
                        epubData.coverImage?.let { coverBytes ->
                            val coverFileName = "cover_${System.currentTimeMillis()}.jpg"
                            val coverFile = File(filesDir, coverFileName)
                            FileOutputStream(coverFile).use { output ->
                                output.write(coverBytes)
                            }
                            coverImagePath = coverFile.absolutePath
                        }
                        
                        // Categorize book
                        val category = bookScanner.categorizeBook(scanned.fileName, scanned.filePath)
                        
                        // Save to database
                        val book = Book(
                            title = epubData.title,
                            author = epubData.author,
                            filePath = scanned.filePath,
                            coverImagePath = coverImagePath,
                            currentPosition = 0,
                            totalPages = epubData.chapters.sumOf { it.content.length },
                            format = BookFormat.EPUB,
                            fileSize = scanned.fileSize,
                            currentChapter = 0,
                            totalChapters = epubData.chapters.size,
                            readingStatus = ReadingStatus.UNREAD,
                            readingProgress = 0f,
                            timeSpentReading = 0L,
                            isFavorite = false,
                            rating = 0,
                            addedTime = System.currentTimeMillis(),
                            lastReadTime = 0L,
                            language = null,
                            publisher = null,
                            description = category, // Using description field to store category
                            isbn = null,
                            publicationYear = null
                        )
                        
                        bookRepository.addBook(book)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Show error to user
            }
        }
    }
    
    @Composable
    private fun LibraryScreenWrapper() {
        var books by remember { mutableStateOf<List<Book>>(emptyList()) }
        var showSettings by remember { mutableStateOf(false) }
        
        // Collect books from repository
        LaunchedEffect(Unit) {
            bookRepository.allBooks.collect { bookList ->
                books = bookList
            }
        }
        
        // File picker launcher
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let { importEpubFile(it) }
        }
        
        if (showSettings) {
            SettingsScreen(onBack = { showSettings = false })
        } else {
            EnhancedLibraryScreen(
                books = books,
                onBookClick = { book ->
                    // Navigate to ReaderActivity
                    startActivity(Intent(this, ReaderActivity::class.java).apply {
                        putExtra("book_id", book.id)
                    })
                },
                onImportBook = {
                    filePickerLauncher.launch(arrayOf("application/epub+zip"))
                },
                onScanBooks = {
                    scanDeviceForBooks()
                },
                onStatusChange = { book, newStatus ->
                    lifecycleScope.launch {
                        bookRepository.updateReadingStatus(book.id, newStatus)
                    }
                },
                onFavoriteToggle = { book ->
                    lifecycleScope.launch {
                        bookRepository.updateFavorite(book.id, !book.isFavorite)
                    }
                },
                onDeleteBook = { book ->
                    lifecycleScope.launch {
                        bookRepository.deleteBook(book)
                    }
                },
                onSettingsClick = {
                    showSettings = true
                }
            )
        }
    }
    
    private fun importEpubFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Copy file to app's internal storage
                val fileName = "book_${System.currentTimeMillis()}.epub"
                val destFile = File(filesDir, fileName)
                
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Parse EPUB to get metadata
                val epubReader = EpubReaderService()
                val epubData = destFile.inputStream().use { 
                    epubReader.loadEpub(it)
                }
                
                // Save cover image if available
                var coverImagePath: String? = null
                epubData.coverImage?.let { coverBytes ->
                    val coverFileName = "cover_${System.currentTimeMillis()}.jpg"
                    val coverFile = File(filesDir, coverFileName)
                    FileOutputStream(coverFile).use { output ->
                        output.write(coverBytes)
                    }
                    coverImagePath = coverFile.absolutePath
                }
                
                // Save to database with enhanced metadata
                val book = Book(
                    title = epubData.title,
                    author = epubData.author,
                    filePath = destFile.absolutePath,
                    coverImagePath = coverImagePath,
                    currentPosition = 0,
                    totalPages = epubData.chapters.sumOf { it.content.length },
                    format = BookFormat.EPUB,
                    fileSize = destFile.length(),
                    currentChapter = 0,
                    totalChapters = epubData.chapters.size,
                    readingStatus = ReadingStatus.UNREAD,
                    readingProgress = 0f,
                    timeSpentReading = 0L,
                    isFavorite = false,
                    rating = 0,
                    addedTime = System.currentTimeMillis(),
                    lastReadTime = 0L,
                    language = null,
                    publisher = null,
                    description = null,
                    isbn = null,
                    publicationYear = null
                )
                
                bookRepository.addBook(book)
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Show error to user
            }
        }
    }
}
