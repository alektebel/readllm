package com.readllm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.readllm.app.database.AppDatabase
import com.readllm.app.llm.TextLLMService
import com.readllm.app.model.Book
import com.readllm.app.model.ColorPreset
import com.readllm.app.quiz.ComprehensionDashboard
import com.readllm.app.quiz.ComprehensionQuizService
import com.readllm.app.quiz.QuizResultsDialog
import com.readllm.app.quiz.QuizScreen
import com.readllm.app.reader.EpubReaderService
import com.readllm.app.repository.BookRepository
import com.readllm.app.repository.BookmarkRepository
import com.readllm.app.repository.QuizRepository
import com.readllm.app.tts.ReadAloudService
import com.readllm.app.ui.HtmlText
import com.readllm.app.ui.reader.ReadingRuler
import com.readllm.app.ui.reader.RSVPOverlay
import com.readllm.app.ui.reader.ParagraphModeOverlay
import com.readllm.app.ui.reader.BionicReading
import com.readllm.app.ui.reader.HorizontalLimiter
import com.readllm.app.ui.reader.PerceptionExpander
import com.readllm.app.ui.reader.ChapterNavigationDrawer
import com.readllm.app.ui.reader.ColorPresetSelector
import com.readllm.app.ui.settings.AppSettings
import com.readllm.app.ui.theme.ReadingThemes
import com.readllm.app.ui.theme.ReadLLMTheme
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

class ReaderActivity : ComponentActivity() {
    
    private lateinit var readAloudService: ReadAloudService
    private lateinit var bookRepository: BookRepository
    private lateinit var quizRepository: QuizRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var epubReader: EpubReaderService
    private lateinit var textLLMService: TextLLMService
    private lateinit var quizService: ComprehensionQuizService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        bookRepository = BookRepository(database.bookDao())
        quizRepository = QuizRepository(database.chapterScoreDao(), database.quizQuestionDao())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())
        epubReader = EpubReaderService()
        
        // Initialize Text LLM Service for quiz generation
        textLLMService = TextLLMService(this)
        quizService = ComprehensionQuizService(textLLMService)
        
        // Initialize LLM model in background
        lifecycleScope.launch {
            textLLMService.initialize()
        }
        
        val bookId = intent.getLongExtra("book_id", -1)
        
        readAloudService = ReadAloudService(this) { status ->
            // Handle status changes
        }
        
        setContent {
            ReadLLMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var book by remember { mutableStateOf<Book?>(null) }
                    var currentChapter by remember { mutableStateOf(0) }
                    var chapterContent by remember { mutableStateOf("") }
                    var totalChapters by remember { mutableStateOf(0) }
                    var chapters by remember { mutableStateOf<List<EpubReaderService.Chapter>>(emptyList()) }
                    var showQuiz by remember { mutableStateOf(false) }
                    var currentQuestion by remember { mutableStateOf<ComprehensionQuizService.QuizQuestion?>(null) }
                    var currentQuestionIndex by remember { mutableStateOf(0) }
                    var quizQuestions by remember { mutableStateOf<List<ComprehensionQuizService.QuizQuestion>>(emptyList()) }
                    var correctAnswersCount by remember { mutableStateOf(0) }
                    var showQuizResults by remember { mutableStateOf(false) }
                    var showDashboard by remember { mutableStateOf(false) }
                    var fontSize by remember { mutableStateOf(18f) }
                    var isPreparingQuiz by remember { mutableStateOf(false) }
                    
                    // Collect settings
                    val context = LocalContext.current
                    val appSettings = remember { AppSettings(context) }
                    val enableAIQuizzes by appSettings.enableAIQuizzes.collectAsState(initial = false)
                    val colorPresetId by appSettings.colorPresetId.collectAsState(initial = 0)
                    val enablePureDark by appSettings.enablePureDark.collectAsState(initial = false)
                    
                    LaunchedEffect(bookId) {
                        if (bookId != -1L) {
                            book = bookRepository.getBookById(bookId)
                            book?.let { loadedBook ->
                                try {
                                    val epubData = File(loadedBook.filePath).inputStream().use {
                                        epubReader.loadEpub(it)
                                    }
                                    totalChapters = epubData.chapters.size
                                    chapters = epubData.chapters
                                    if (epubData.chapters.isNotEmpty()) {
                                        chapterContent = epubData.chapters[0].content
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    chapterContent = "Error loading book: ${e.message}"
                                }
                            }
                        }
                    }

/**
 * Loading screen shown while AI prepares comprehension questions
 */
@Composable
fun QuizPreparationScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Preparing Questions...",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "The AI is analyzing this chapter to create personalized comprehension questions for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 16.dp)
            )
        }
    }
}
                    
                    if (showDashboard) {
                        var chapterScores by remember { mutableStateOf(emptyList<com.readllm.app.model.ChapterScoreEntity>()) }
                        
                        LaunchedEffect(bookId) {
                            if (bookId != -1L) {
                                quizRepository.getChapterScores(bookId).collect { scores ->
                                    chapterScores = scores
                                }
                            }
                        }
                        
                        ComprehensionDashboard(
                            bookTitle = book?.title ?: "Book",
                            chapterScores = chapterScores,
                            onClose = { showDashboard = false }
                        )
                    } else if (isPreparingQuiz) {
                        // Loading screen while AI prepares questions
                        QuizPreparationScreen()
                    } else if (showQuiz && currentQuestion != null) {
                        Dialog(onDismissRequest = { /* Prevent dismissal during quiz */ }) {
                            QuizScreen(
                                question = currentQuestion!!,
                                chapterContent = chapterContent,
                                quizService = quizService,
                                onAnswerSelected = { answer ->
                                    if (answer == currentQuestion!!.correctAnswer) {
                                        correctAnswersCount++
                                    }
                                },
                                onDismiss = {
                                    if (currentQuestionIndex < quizQuestions.size - 1) {
                                        // Move to next question
                                        currentQuestionIndex++
                                        currentQuestion = quizQuestions[currentQuestionIndex]
                                    } else {
                                        // Quiz complete
                                        showQuiz = false
                                        showQuizResults = true
                                        
                                        // Save scores to database
                                        lifecycleScope.launch {
                                            if (bookId != -1L) {
                                                quizRepository.saveChapterScore(
                                                    bookId = bookId,
                                                    chapterNumber = currentChapter,
                                                    correctAnswers = correctAnswersCount,
                                                    totalQuestions = quizQuestions.size,
                                                    averageDifficulty = quizQuestions.map { it.difficulty }.average()
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        ReaderScreen(
                            book = book,
                            chapterContent = chapterContent,
                            currentChapter = currentChapter,
                            totalChapters = totalChapters,
                            chapters = chapters,
                            fontSize = fontSize,
                            onFontSizeChange = { newSize ->
                                fontSize = newSize
                            },
                            onReadAloudClick = { segments ->
                                readAloudService.readAloud(segments)
                            },
                            onPauseClick = {
                                readAloudService.pause()
                            },
                            onStopClick = {
                                readAloudService.stop()
                            },
                            onChapterChange = { newChapter ->
                                currentChapter = newChapter
                                lifecycleScope.launch {
                                    book?.let { loadedBook ->
                                        val epubData = File(loadedBook.filePath).inputStream().use {
                                            epubReader.loadEpub(it)
                                        }
                                        if (newChapter < epubData.chapters.size) {
                                            chapterContent = epubData.chapters[newChapter].content
                                            
                                             // Check if we should show quiz at chapter end (only if enabled)
                                             if (enableAIQuizzes && quizService.shouldShowQuiz(chapterContent, newChapter)) {
                                                // Show loading screen while AI prepares questions
                                                isPreparingQuiz = true
                                                
                                                // Use simple default score for now
                                                val avgScore = 75
                                                
                                                // Generate questions using AI
                                                quizQuestions = quizService.generateQuestions(
                                                    chapterContent = chapterContent,
                                                    chapterNumber = newChapter,
                                                    previousScore = avgScore
                                                )
                                                
                                                isPreparingQuiz = false
                                                
                                                if (quizQuestions.isNotEmpty()) {
                                                    currentQuestionIndex = 0
                                                    currentQuestion = quizQuestions[0]
                                                    correctAnswersCount = 0
                                                    showQuiz = true
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onShowDashboard = {
                                showDashboard = true
                            }
                        )
                    }
                    
                    // Quiz results dialog
                    if (showQuizResults) {
                        QuizResultsDialog(
                            correctAnswers = correctAnswersCount,
                            totalQuestions = quizQuestions.size,
                            onDismiss = {
                                showQuizResults = false
                                correctAnswersCount = 0
                            }
                        )
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        readAloudService.cleanup()
        textLLMService.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: Book?,
    chapterContent: String,
    currentChapter: Int,
    totalChapters: Int,
    chapters: List<EpubReaderService.Chapter>,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    onReadAloudClick: (List<ReadAloudService.ContentSegment>) -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onChapterChange: (Int) -> Unit,
    onShowDashboard: () -> Unit
) {
    // Get app settings
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    
    // Collect typography settings
    val lineHeight by appSettings.lineHeight.collectAsState(initial = 1.5f)
    val letterSpacing by appSettings.letterSpacing.collectAsState(initial = 0f)
    val paragraphSpacing by appSettings.paragraphSpacing.collectAsState(initial = 16f)
    val textIndent by appSettings.textIndent.collectAsState(initial = 0f)
    
    // Collect reading mode settings
    val enableReadingRuler by appSettings.enableReadingRuler.collectAsState(initial = false)
    val enableRSVPMode by appSettings.enableRSVPMode.collectAsState(initial = false)
    val enableParagraphMode by appSettings.enableParagraphMode.collectAsState(initial = false)
    
    // Collect book-story inspired features
    val enableBionicReading by appSettings.enableBionicReading.collectAsState(initial = false)
    val enableHorizontalLimiter by appSettings.enableHorizontalLimiter.collectAsState(initial = false)
    val horizontalLimiterHeight by appSettings.horizontalLimiterHeight.collectAsState(initial = 100f)
    val horizontalLimiterOffset by appSettings.horizontalLimiterOffset.collectAsState(initial = 0f)
    val enablePerceptionExpander by appSettings.enablePerceptionExpander.collectAsState(initial = false)
    val perceptionExpanderPadding by appSettings.perceptionExpanderPadding.collectAsState(initial = 80f)
    
    // Collect theme settings
    val readingThemeName by appSettings.readingTheme.collectAsState(initial = "Default Light")
    val readingTheme = remember(readingThemeName) { ReadingThemes.getThemeByName(readingThemeName) }
    
    // Collect color preset setting
    val colorPresetId by appSettings.colorPresetId.collectAsState(initial = 0)
    val colorPreset = remember(colorPresetId) { ColorPreset.getPresetById(colorPresetId) }
    
    // Coroutine scope for settings updates
    val scope = rememberCoroutineScope()
    
    var isReadAloudMode by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var speechRate by remember { mutableStateOf(1.0f) }
    
    // Reading mode states
    var showRSVP by remember { mutableStateOf(false) }
    var showParagraphMode by remember { mutableStateOf(false) }
    var showChapterNavigation by remember { mutableStateOf(false) }
    var showColorPresets by remember { mutableStateOf(false) }
    
    // Swipe gesture state
    var dragOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 300f
    
    // Convert HTML to AnnotatedString for paragraph mode
    val annotatedText = remember(chapterContent) {
        androidx.compose.ui.text.buildAnnotatedString {
            append(chapterContent)
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Column {
                            Text(book?.title ?: "Loading...")
                            if (totalChapters > 0 && chapters.isNotEmpty()) {
                                Text(
                                    text = chapters.getOrNull(currentChapter)?.title ?: "Chapter ${currentChapter + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Navigate back */ }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Color preset button
                        IconButton(onClick = { showColorPresets = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Change Colors")
                        }
                        
                        // Chapter navigation button
                        IconButton(onClick = { showChapterNavigation = true }) {
                            Icon(Icons.Default.List, contentDescription = "Table of Contents")
                        }
                    
                    // Reading modes menu
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Reading Modes")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (enableRSVPMode) {
                            DropdownMenuItem(
                                text = { Text("RSVP Speed Reading") },
                                onClick = {
                                    showRSVP = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Speed, null) }
                            )
                        }
                        if (enableParagraphMode) {
                            DropdownMenuItem(
                                text = { Text("Paragraph Focus Mode") },
                                onClick = {
                                    showParagraphMode = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.FormatAlignLeft, null) }
                            )
                        }
                    }
                    
                    IconButton(onClick = onShowDashboard) {
                        Icon(Icons.Default.Analytics, contentDescription = "Comprehension Dashboard")
                    }
                    IconButton(onClick = { isReadAloudMode = !isReadAloudMode }) {
                        Icon(
                            if (isReadAloudMode) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Read Aloud"
                        )
                    }
                }
            )
            
            // Chapter progress indicator
            if (totalChapters > 0) {
                LinearProgressIndicator(
                    progress = (currentChapter + 1).toFloat() / totalChapters,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        },
        floatingActionButton = {
            // Only show FAB when not in read-aloud mode
            if (!isReadAloudMode && totalChapters > 0) {
                FloatingActionButton(
                    onClick = { showChapterNavigation = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Table of Contents")
                }
            }
        },
        bottomBar = {
            if (isReadAloudMode) {
                ReadAloudControls(
                    isPlaying = isPlaying,
                    speechRate = speechRate,
                    onPlayPauseClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            // Create content segments from chapter
                            val segments = listOf(
                                ReadAloudService.ContentSegment(
                                    text = chapterContent,
                                    position = 0
                                )
                            )
                            onReadAloudClick(segments)
                        } else {
                            onPauseClick()
                        }
                    },
                    onStopClick = {
                        isPlaying = false
                        onStopClick()
                    },
                    onSpeedChange = { newRate ->
                        speechRate = newRate
                    }
                )
            } else {
                // Chapter navigation
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                if (currentChapter > 0) {
                                    onChapterChange(currentChapter - 1)
                                }
                            },
                            enabled = currentChapter > 0
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = null)
                            Text("Previous")
                        }
                        
                        Button(
                            onClick = { 
                                if (currentChapter < totalChapters - 1) {
                                    onChapterChange(currentChapter + 1)
                                }
                            },
                            enabled = currentChapter < totalChapters - 1
                        ) {
                            Text("Next")
                            Icon(Icons.Default.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content with theme
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(dragOffset) > swipeThreshold) {
                                    if (dragOffset > 0 && currentChapter > 0) {
                                        // Swipe right - previous chapter
                                        onChapterChange(currentChapter - 1)
                                    } else if (dragOffset < 0 && currentChapter < totalChapters - 1) {
                                        // Swipe left - next chapter
                                        onChapterChange(currentChapter + 1)
                                    }
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                            }
                        )
                    }
            ) {
                if (book != null) {
                    HtmlText(
                        html = chapterContent.ifEmpty { "<p>Loading chapter...</p>" },
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        paragraphSpacing = paragraphSpacing,
                        textIndent = textIndent,
                        textColor = colorPreset.textColor,
                        backgroundColor = colorPreset.backgroundColor,
                        enableBionicReading = enableBionicReading,
                        onFontSizeChange = onFontSizeChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp)
                    )
                }
            }
            
            // Horizontal Limiter (Focus Zone)
            if (enableHorizontalLimiter && !showRSVP && !showParagraphMode) {
                HorizontalLimiter(
                    enabled = true,
                    zoneHeight = horizontalLimiterHeight.dp,
                    verticalOffset = horizontalLimiterOffset.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Perception Expander (Eye Tracking Guides)
            if (enablePerceptionExpander && !showRSVP && !showParagraphMode) {
                PerceptionExpander(
                    enabled = true,
                    horizontalPadding = perceptionExpanderPadding.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Reading Ruler overlay
            if (enableReadingRuler && !showRSVP && !showParagraphMode) {
                ReadingRuler(
                    enabled = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // RSVP overlay
            if (showRSVP) {
                RSVPOverlay(
                    text = chapterContent,
                    isActive = true,
                    onDismiss = { showRSVP = false }
                )
            }
            
            // Paragraph mode overlay
            if (showParagraphMode) {
                ParagraphModeOverlay(
                    text = annotatedText,
                    isActive = true,
                    fontSize = fontSize,
                    onDismiss = { showParagraphMode = false }
                )
            }
            
            // Chapter navigation drawer
            if (showChapterNavigation && chapters.isNotEmpty()) {
                ChapterNavigationDrawer(
                    chapters = chapters,
                    currentChapterIndex = currentChapter,
                    onChapterSelected = { chapterIndex ->
                        onChapterChange(chapterIndex)
                    },
                    onDismiss = { showChapterNavigation = false }
                )
            }
            
            // Color preset selector
            if (showColorPresets) {
                ColorPresetSelector(
                    selectedPresetId = colorPresetId,
                    onPresetSelected = { preset ->
                        scope.launch {
                            appSettings.setColorPresetId(preset.id)
                        }
                        showColorPresets = false
                    },
                    onDismiss = { showColorPresets = false }
                )
            }
        }
    }
}

@Composable
fun ReadAloudControls(
    isPlaying: Boolean,
    speechRate: Float,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onStopClick) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
                
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = "${speechRate}x",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speed:", modifier = Modifier.padding(end = 8.dp))
                Slider(
                    value = speechRate,
                    onValueChange = onSpeedChange,
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
