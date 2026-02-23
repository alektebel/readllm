package com.readllm.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import com.readllm.app.auth.GitHubAuthService
import com.readllm.app.database.AppDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
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
    private lateinit var githubAuthService: GitHubAuthService
    
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
        githubAuthService = GitHubAuthService(this)
        
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
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        // Handle OAuth redirect
        intent?.data?.let { uri ->
            if (uri.scheme == "com.readllm.app" && uri.host == "oauth") {
                handleOAuthCallback(intent)
            }
        }
    }
    
    private fun scanDeviceForBooks() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val scannedBooks = bookScanner.scanForBooks()
                
                // Get existing books once using first() instead of collect
                val existingBooks = bookRepository.allBooks.first()
                
                scannedBooks.forEach { scanned ->
                    // Check if book already exists in database
                    val alreadyExists = existingBooks.any { it.filePath == scanned.filePath }
                    
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
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error scanning books: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    @Composable
    private fun LibraryScreenWrapper() {
        var books by remember { mutableStateOf<List<Book>>(emptyList()) }
        var showSettings by remember { mutableStateOf(false) }
        
        // OAuth launcher
        val oauthLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { handleOAuthCallback(it) }
            }
        }
        
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
            SettingsScreen(
                onBack = { showSettings = false },
                onGitHubSignIn = {
                    startGitHubOAuth(oauthLauncher)
                },
                onGitHubSignOut = {
                    handleGitHubSignOut()
                }
            )
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
    
    private fun startGitHubOAuth(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val authRequest = githubAuthService.buildAuthorizationRequest()
        val authIntent = githubAuthService.getAuthorizationIntent(authRequest)
        launcher.launch(authIntent)
    }
    
    private fun handleOAuthCallback(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        
        lifecycleScope.launch {
            when {
                response != null -> {
                    val result = githubAuthService.handleAuthResponse(response)
                    if (result.isSuccess) {
                        Toast.makeText(
                            this@MainActivity,
                            "Successfully signed in with GitHub!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to authenticate: ${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                exception != null -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication cancelled or failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun handleGitHubSignOut() {
        githubAuthService.clearAuth()
        Toast.makeText(
            this,
            "Signed out from GitHub",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun importEpubFile(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Validate file type
                val mimeType = contentResolver.getType(uri)
                if (mimeType != "application/epub+zip" && !uri.toString().endsWith(".epub", ignoreCase = true)) {
                    showError("Invalid file type. Please select an EPUB file.")
                    return@launch
                }
                
                // Validate file size (max 100MB to prevent DoS)
                val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                val fileSize = fileDescriptor?.statSize ?: 0
                fileDescriptor?.close()
                
                if (fileSize > 100 * 1024 * 1024) {
                    showError("File too large. Maximum size is 100MB.")
                    return@launch
                }
                
                if (fileSize == 0L) {
                    showError("File is empty or cannot be read.")
                    return@launch
                }
                
                // Generate safe filename (prevent path traversal)
                val fileName = "book_${System.currentTimeMillis()}.epub"
                val destFile = File(filesDir, fileName)
                
                // Ensure file is created in app's directory (not arbitrary location)
                if (!destFile.canonicalPath.startsWith(filesDir.canonicalPath)) {
                    showError("Invalid file path.")
                    return@launch
                }
                
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: run {
                    showError("Cannot read file. Please check permissions.")
                    return@launch
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
                
                // Show success message
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Book imported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Failed to import book: ${e.message}")
            }
        }
    }
    
    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                this@MainActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
