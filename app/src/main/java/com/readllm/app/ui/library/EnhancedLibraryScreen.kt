package com.readllm.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readllm.app.model.Book
import com.readllm.app.model.ReadingStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class ViewMode {
    LIST,
    GRID
}

enum class SortBy {
    TITLE,
    AUTHOR,
    DATE_ADDED,
    LAST_READ,
    PROGRESS,
    FILE_SIZE,
    CATEGORY
}

data class LibraryFilter(
    val status: ReadingStatus? = null,
    val favorites: Boolean = false,
    val searchQuery: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedLibraryScreen(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onImportBook: () -> Unit,
    onScanBooks: () -> Unit = {},
    onBookLongPress: (Book) -> Unit = {},
    onStatusChange: (Book, ReadingStatus) -> Unit = { _, _ -> },
    onFavoriteToggle: (Book) -> Unit = {},
    onDeleteBook: (Book) -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortBy by remember { mutableStateOf(SortBy.LAST_READ) }
    var filter by remember { mutableStateOf(LibraryFilter()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Filter and sort books
    val filteredBooks = remember(books, filter, sortBy, searchQuery) {
        books.filter { book ->
            val matchesStatus = filter.status == null || book.readingStatus == filter.status
            val matchesFavorites = !filter.favorites || book.isFavorite
            val matchesSearch = searchQuery.isEmpty() || 
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true)
            
            matchesStatus && matchesFavorites && matchesSearch
        }.sortedWith(
            when (sortBy) {
                SortBy.TITLE -> compareBy { it.title }
                SortBy.AUTHOR -> compareBy { it.author }
                SortBy.DATE_ADDED -> compareByDescending { it.addedTime }
                SortBy.LAST_READ -> compareByDescending { it.lastReadTime }
                SortBy.FILE_SIZE -> compareByDescending { it.fileSize }
                SortBy.PROGRESS -> compareByDescending { it.readingProgress }
                SortBy.CATEGORY -> compareBy { it.description ?: "Uncategorized" }
            }
        )
    }
    
    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {},
                    active = true,
                    onActiveChange = { if (!it) isSearchActive = false },
                    placeholder = { Text("Search books...") },
                    leadingIcon = {
                        IconButton(onClick = { isSearchActive = false }) {
                            Icon(Icons.Default.ArrowBack, "Close search")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    }
                ) {}
            } else {
                TopAppBar(
                    title = { Text("Library (${filteredBooks.size})") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, "Sort")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                        IconButton(onClick = {
                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                        }) {
                            Icon(
                                if (viewMode == ViewMode.LIST) Icons.Default.GridView 
                                else Icons.Default.List,
                                "Toggle view"
                            )
                        }
                        IconButton(onClick = onScanBooks) {
                            Icon(Icons.Default.CloudDownload, "Scan device for books")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(onClick = onScanBooks) {
                    Icon(Icons.Default.FolderOpen, "Scan for books")
                }
                FloatingActionButton(onClick = onImportBook) {
                    Icon(Icons.Default.Add, "Add book")
                }
            }
        }
    ) { padding ->
        if (filteredBooks.isEmpty()) {
            EmptyLibraryView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                hasBooks = books.isNotEmpty(),
                onImportClick = onImportBook
            )
        } else {
            when (viewMode) {
                ViewMode.LIST -> {
                    BookListView(
                        books = filteredBooks,
                        onBookClick = onBookClick,
                        onBookLongPress = onBookLongPress,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier.padding(padding)
                    )
                }
                ViewMode.GRID -> {
                    BookGridView(
                        books = filteredBooks,
                        onBookClick = onBookClick,
                        onBookLongPress = onBookLongPress,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = filter,
            onFilterChange = { filter = it },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = sortBy,
            onSortChange = { sortBy = it },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun BookListView(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onBookLongPress: (Book) -> Unit,
    onFavoriteToggle: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookListItem(
                book = book,
                onClick = { onBookClick(book) },
                onLongPress = { onBookLongPress(book) },
                onFavoriteToggle = { onFavoriteToggle(book) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Status indicator
                    StatusChip(status = book.readingStatus)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                if (book.readingProgress > 0) {
                    LinearProgressIndicator(
                        progress = book.readingProgress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${book.readingProgress.toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Additional info
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = formatFileSize(book.fileSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = book.format.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Favorite button
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (book.isFavorite) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookGridView(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onBookLongPress: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookGridItem(
                book = book,
                onClick = { onBookClick(book) },
                onLongPress = { onBookLongPress(book) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookGridItem(
    book: Book,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Book cover placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (book.coverImagePath != null && File(book.coverImagePath).exists()) {
                    AsyncImage(
                        model = File(book.coverImagePath),
                        contentDescription = "Book cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (book.readingProgress > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = book.readingProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = book.readingStatus, compact = true)
                
                if (book.isFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    status: ReadingStatus,
    compact: Boolean = false
) {
    val (text, color) = when (status) {
        ReadingStatus.UNREAD -> "New" to MaterialTheme.colorScheme.tertiary
        ReadingStatus.READING -> "Reading" to MaterialTheme.colorScheme.primary
        ReadingStatus.FINISHED -> "Finished" to MaterialTheme.colorScheme.secondary
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelSmall 
                    else MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyLibraryView(
    modifier: Modifier = Modifier,
    hasBooks: Boolean,
    onImportClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (hasBooks) "No books match your filters" 
                       else "Your library is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!hasBooks) {
                Button(onClick = onImportClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add your first book")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var filter by remember { mutableStateOf(currentFilter) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Books") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", style = MaterialTheme.typography.titleSmall)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter.status == null,
                        onClick = { filter = filter.copy(status = null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = filter.status == ReadingStatus.UNREAD,
                        onClick = { filter = filter.copy(status = ReadingStatus.UNREAD) },
                        label = { Text("Unread") }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter.status == ReadingStatus.READING,
                        onClick = { filter = filter.copy(status = ReadingStatus.READING) },
                        label = { Text("Reading") }
                    )
                    FilterChip(
                        selected = filter.status == ReadingStatus.FINISHED,
                        onClick = { filter = filter.copy(status = ReadingStatus.FINISHED) },
                        label = { Text("Finished") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Favorites only")
                    Switch(
                        checked = filter.favorites,
                        onCheckedChange = { filter = filter.copy(favorites = it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onFilterChange(filter)
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSort: SortBy,
    onSortChange: (SortBy) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Books") },
        text = {
            Column {
                SortBy.values().forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == sortOption,
                            onClick = {
                                onSortChange(sortOption)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (sortOption) {
                                SortBy.TITLE -> "Title"
                                SortBy.AUTHOR -> "Author"
                                SortBy.DATE_ADDED -> "Date Added"
                                SortBy.LAST_READ -> "Last Read"
                                SortBy.FILE_SIZE -> "File Size"
                                SortBy.PROGRESS -> "Progress"
                                SortBy.CATEGORY -> "Category"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
