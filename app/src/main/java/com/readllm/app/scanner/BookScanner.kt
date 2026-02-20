package com.readllm.app.scanner

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Book Scanner Service
 * 
 * Scans device storage for EPUB, PDF, and other book files
 */
class BookScanner(private val context: Context) {
    
    data class ScannedBook(
        val filePath: String,
        val fileName: String,
        val fileSize: Long,
        val lastModified: Long
    )
    
    /**
     * Scan device for book files
     * Uses MediaStore for Android 10+ and file system scan for older versions
     */
    suspend fun scanForBooks(): List<ScannedBook> = withContext(Dispatchers.IO) {
        val books = mutableListOf<ScannedBook>()
        
        // Scan using MediaStore (works on all Android versions)
        scanUsingMediaStore(books)
        
        // Additionally scan common directories
        scanCommonDirectories(books)
        
        books.distinctBy { it.filePath }
    }
    
    /**
     * Scan using Android MediaStore API
     */
    private fun scanUsingMediaStore(books: MutableList<ScannedBook>) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        
        // Query for EPUB files
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR " +
                       "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("application/epub+zip", "%.epub")
        
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(pathColumn)
                val size = cursor.getLong(sizeColumn)
                val modified = cursor.getLong(modifiedColumn)
                
                if (path != null && File(path).exists()) {
                    books.add(ScannedBook(path, name, size, modified * 1000))
                }
            }
        }
    }
    
    /**
     * Scan common book storage directories
     */
    private fun scanCommonDirectories(books: MutableList<ScannedBook>) {
        val directories = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            File(Environment.getExternalStorageDirectory(), "Books"),
            File(Environment.getExternalStorageDirectory(), "eBooks"),
            File(Environment.getExternalStorageDirectory(), "Download")
        )
        
        directories.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, books)
            }
        }
    }
    
    /**
     * Recursively scan a directory for book files
     */
    private fun scanDirectory(directory: File, books: MutableList<ScannedBook>, maxDepth: Int = 3) {
        if (maxDepth <= 0) return
        
        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isFile && isBookFile(file) -> {
                        books.add(ScannedBook(
                            filePath = file.absolutePath,
                            fileName = file.name,
                            fileSize = file.length(),
                            lastModified = file.lastModified()
                        ))
                    }
                    file.isDirectory && !file.name.startsWith(".") -> {
                        scanDirectory(file, books, maxDepth - 1)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Skip directories we don't have permission to read
        }
    }
    
    /**
     * Check if file is a supported book format
     */
    private fun isBookFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("epub", "pdf", "mobi", "azw", "azw3", "fb2", "txt")
    }
    
    /**
     * Categorize book by analyzing filename and metadata
     * Returns a category guess based on keywords
     */
    fun categorizeBook(fileName: String, filePath: String): String {
        val lowerName = fileName.lowercase()
        
        return when {
            lowerName.contains("fiction") || lowerName.contains("novel") -> "Fiction"
            lowerName.contains("science") && !lowerName.contains("fiction") -> "Science"
            lowerName.contains("history") -> "History"
            lowerName.contains("biography") || lowerName.contains("memoir") -> "Biography"
            lowerName.contains("tech") || lowerName.contains("programming") || 
            lowerName.contains("computer") -> "Technology"
            lowerName.contains("business") || lowerName.contains("management") -> "Business"
            lowerName.contains("art") || lowerName.contains("design") -> "Art & Design"
            lowerName.contains("cook") || lowerName.contains("recipe") -> "Cooking"
            lowerName.contains("health") || lowerName.contains("medical") -> "Health"
            lowerName.contains("self-help") || lowerName.contains("motivation") -> "Self-Help"
            lowerName.contains("religion") || lowerName.contains("spiritual") -> "Religion"
            lowerName.contains("travel") -> "Travel"
            lowerName.contains("child") || lowerName.contains("kid") -> "Children"
            lowerName.contains("fantasy") -> "Fantasy"
            lowerName.contains("mystery") || lowerName.contains("thriller") -> "Mystery & Thriller"
            lowerName.contains("romance") -> "Romance"
            else -> "Uncategorized"
        }
    }
}
