package com.readllm.app.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * EPUB Reader Service
 * 
 * Parses EPUB files manually using ZIP and XML parsing.
 * EPUB files are ZIP archives containing XHTML content and metadata.
 */
class EpubReaderService {
    
    data class EpubContent(
        val text: String,
        val images: List<ImageContent>
    )
    
    data class ImageContent(
        val bitmap: Bitmap,
        val alt: String?,
        val position: Int
    )
    
    data class EpubBook(
        val title: String,
        val author: String,
        val chapters: List<Chapter>,
        val images: Map<String, ByteArray>,
        val coverImage: ByteArray? = null
    )
    
    data class Chapter(
        val title: String,
        val content: String,
        val order: Int
    )
    
    data class BookMetadata(
        val title: String,
        val author: String,
        val description: String
    )
    
    /**
     * Load EPUB file from input stream
     */
    fun loadEpub(inputStream: InputStream): EpubBook {
        val entries = mutableMapOf<String, ByteArray>()
        val zipInputStream = ZipInputStream(inputStream)
        
        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                val bytes = zipInputStream.readBytes()
                entries[entry.name] = bytes
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        
        // Parse metadata
        val metadata = parseMetadata(entries)
        
        // Parse chapters
        val chapters = parseChapters(entries)
        
        // Extract images
        val images = extractImages(entries)
        
        // Extract cover image
        val coverImage = extractCoverImage(entries, metadata)
        
        return EpubBook(
            title = metadata.title,
            author = metadata.author,
            chapters = chapters,
            images = images,
            coverImage = coverImage
        )
    }
    
    fun getChapterContent(book: EpubBook, chapterIndex: Int): EpubContent {
        if (chapterIndex >= book.chapters.size) {
            return EpubContent("", emptyList())
        }
        
        val chapter = book.chapters[chapterIndex]
        val images = extractImagesFromChapter(chapter.content, book.images)
        val cleanText = cleanHtmlContent(chapter.content)
        
        return EpubContent(cleanText, images)
    }
    
    fun getChapterCount(book: EpubBook): Int {
        return book.chapters.size
    }
    
    fun getBookMetadata(book: EpubBook): BookMetadata {
        return BookMetadata(
            title = book.title,
            author = book.author,
            description = ""
        )
    }
    
    private fun parseMetadata(entries: Map<String, ByteArray>): BookMetadata {
        // Find and parse content.opf
        val opfEntry = entries.entries.find { it.key.endsWith(".opf") || it.key.contains("content.opf") }
        
        if (opfEntry != null) {
            try {
                val doc = parseXml(opfEntry.value.inputStream())
                val title = doc.getElementsByTagName("dc:title").item(0)?.textContent ?: "Unknown"
                val author = doc.getElementsByTagName("dc:creator").item(0)?.textContent ?: "Unknown"
                
                return BookMetadata(title, author, "")
            } catch (e: Exception) {
                // Fallback to defaults
            }
        }
        
        return BookMetadata("Unknown", "Unknown", "")
    }
    
    private fun parseChapters(entries: Map<String, ByteArray>): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        
        // Find HTML/XHTML files (typical EPUB chapter files)
        val htmlFiles = entries.filter { (key, _) ->
            key.endsWith(".html") || key.endsWith(".xhtml") || key.endsWith(".htm")
        }.entries.sortedBy { it.key }
        
        htmlFiles.forEachIndexed { index, entry ->
            try {
                val content = String(entry.value, Charsets.UTF_8)
                val title = extractTitle(content) ?: "Chapter ${index + 1}"
                
                chapters.add(Chapter(
                    title = title,
                    content = content,
                    order = index
                ))
            } catch (e: Exception) {
                // Skip malformed chapters
            }
        }
        
        return chapters
    }
    
    private fun extractImages(entries: Map<String, ByteArray>): Map<String, ByteArray> {
        return entries.filter { (key, _) ->
            key.endsWith(".jpg") || key.endsWith(".jpeg") || 
            key.endsWith(".png") || key.endsWith(".gif") ||
            key.endsWith(".webp")
        }
    }
    
    private fun extractCoverImage(entries: Map<String, ByteArray>, metadata: BookMetadata): ByteArray? {
        // Strategy 1: Look for common cover image filenames
        val coverKeywords = listOf("cover", "Cover", "COVER", "front", "Front")
        for (keyword in coverKeywords) {
            entries.entries.find { 
                it.key.contains(keyword) && 
                (it.key.endsWith(".jpg") || it.key.endsWith(".jpeg") || 
                 it.key.endsWith(".png") || it.key.endsWith(".gif") ||
                 it.key.endsWith(".webp"))
            }?.value?.let { return it }
        }
        
        // Strategy 2: Parse OPF file for cover reference
        val opfEntry = entries.entries.find { 
            it.key.endsWith(".opf") || it.key.contains("content.opf") 
        }
        
        if (opfEntry != null) {
            try {
                val doc = parseXml(opfEntry.value.inputStream())
                
                // Look for <meta name="cover" content="cover-image"/>
                val metaTags = doc.getElementsByTagName("meta")
                for (i in 0 until metaTags.length) {
                    val meta = metaTags.item(i) as? Element
                    if (meta?.getAttribute("name") == "cover") {
                        val coverId = meta.getAttribute("content")
                        
                        // Find manifest item with this ID
                        val manifestItems = doc.getElementsByTagName("item")
                        for (j in 0 until manifestItems.length) {
                            val item = manifestItems.item(j) as? Element
                            if (item?.getAttribute("id") == coverId) {
                                val href = item?.getAttribute("href") ?: continue
                                // Find the image with this path
                                val basePath = opfEntry.key.substringBeforeLast("/")
                                val imagePath = if (basePath.isNotEmpty()) "$basePath/$href" else href
                                entries[imagePath]?.let { return it }
                                
                                // Try relative path resolution
                                entries.entries.find { it.key.endsWith(href) }?.value?.let { return it }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to strategy 3
            }
        }
        
        // Strategy 3: Return first image in the book
        return entries.filter { (key, _) ->
            key.endsWith(".jpg") || key.endsWith(".jpeg") || 
            key.endsWith(".png") || key.endsWith(".gif") ||
            key.endsWith(".webp")
        }.entries.firstOrNull()?.value
    }
    
    private fun extractImagesFromChapter(htmlContent: String, imageMap: Map<String, ByteArray>): List<ImageContent> {
        val images = mutableListOf<ImageContent>()
        val imgRegex = Regex("<img[^>]+src=\"([^\"]+)\"[^>]*(?:alt=\"([^\"]*)\")?[^>]*>", RegexOption.IGNORE_CASE)
        
        imgRegex.findAll(htmlContent).forEachIndexed { index, match ->
            val src = match.groupValues[1]
            val alt = match.groupValues.getOrNull(2)
            
            // Find the image in the map
            val imageBytes = findImageByPath(src, imageMap)
            imageBytes?.let { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    images.add(ImageContent(bitmap, alt, match.range.first))
                }
            }
        }
        
        return images
    }
    
    private fun findImageByPath(path: String, imageMap: Map<String, ByteArray>): ByteArray? {
        // Try direct match
        imageMap[path]?.let { return it }
        
        // Try with cleaned path
        val cleanPath = path.removePrefix("/").removePrefix("../").removePrefix("./")
        imageMap.entries.find { it.key.endsWith(cleanPath) }?.value?.let { return it }
        
        // Try filename only
        val filename = path.substringAfterLast("/")
        imageMap.entries.find { it.key.endsWith(filename) }?.value?.let { return it }
        
        return null
    }
    
    private fun extractTitle(html: String): String? {
        val titleRegex = Regex("<title>([^<]+)</title>", RegexOption.IGNORE_CASE)
        val h1Regex = Regex("<h1[^>]*>([^<]+)</h1>", RegexOption.IGNORE_CASE)
        
        titleRegex.find(html)?.groupValues?.get(1)?.let { return it.trim() }
        h1Regex.find(html)?.groupValues?.get(1)?.let { return it.trim() }
        
        return null
    }
    
    private fun cleanHtmlContent(html: String): String {
        // Remove XML declarations, DOCTYPE, and namespace attributes
        var cleaned = html
            .replace(Regex("<\\?xml[^>]*\\?>"), "")
            .replace(Regex("<!DOCTYPE[^>]*>"), "")
            .replace(Regex("xmlns[^\"]*\"[^\"]*\""), "")
            .replace(Regex("<html[^>]*>"), "<html>")
            .replace(Regex("<head>.*?</head>", RegexOption.DOT_MATCHES_ALL), "")
        
        // Remove style and script tags
        cleaned = cleaned
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
        
        // Extract body content if present
        val bodyMatch = Regex("<body[^>]*>(.*?)</body>", RegexOption.DOT_MATCHES_ALL).find(cleaned)
        cleaned = bodyMatch?.groupValues?.get(1) ?: cleaned
        
        // Clean up namespace prefixes and epub-specific tags
        cleaned = cleaned
            .replace(Regex("</?epub:[^>]*>"), "")
            .replace(Regex("epub:type=\"[^\"]*\""), "")
        
        // Decode HTML entities (common ones first, then numeric entities)
        cleaned = cleaned
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&lsquo;", "'")
            .replace("&rsquo;", "'")
            .replace("&ldquo;", """)
            .replace("&rdquo;", """)
            .replace("&hellip;", "…")
            // Decode numeric HTML entities (&#123; or &#x7B;)
            .replace(Regex("&#x([0-9a-fA-F]+);")) { matchResult ->
                matchResult.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: matchResult.value
            }
            .replace(Regex("&#(\\d+);")) { matchResult ->
                matchResult.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: matchResult.value
            }
        
        return cleaned.trim()
    }
    
    private fun parseXml(inputStream: InputStream): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        return builder.parse(inputStream)
    }
}
