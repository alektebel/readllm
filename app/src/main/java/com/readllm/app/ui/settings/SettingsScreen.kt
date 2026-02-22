package com.readllm.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * App settings with DataStore persistence
 */
class AppSettings(private val context: Context) {
    
    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_HEIGHT = floatPreferencesKey("line_height")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val PARAGRAPH_SPACING = floatPreferencesKey("paragraph_spacing")
        val TEXT_INDENT = floatPreferencesKey("text_indent")
        val ENABLE_AI_QUIZZES = booleanPreferencesKey("enable_ai_quizzes")
        val AUTO_DOWNLOAD_MODEL = booleanPreferencesKey("auto_download_model")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val READING_THEME = stringPreferencesKey("reading_theme")
        val QUIZ_DIFFICULTY = intPreferencesKey("quiz_difficulty")
        val QUESTIONS_PER_CHAPTER = intPreferencesKey("questions_per_chapter")
        val ENABLE_SWIPE_NAVIGATION = booleanPreferencesKey("enable_swipe_navigation")
        val PAGE_TURN_ANIMATION = booleanPreferencesKey("page_turn_animation")
        val AUTO_SAVE_PROGRESS = booleanPreferencesKey("auto_save_progress")
        val ENABLE_READING_RULER = booleanPreferencesKey("enable_reading_ruler")
        val ENABLE_RSVP_MODE = booleanPreferencesKey("enable_rsvp_mode")
        val ENABLE_PARAGRAPH_MODE = booleanPreferencesKey("enable_paragraph_mode")
        val TTS_HIGHLIGHT_SENTENCES = booleanPreferencesKey("tts_highlight_sentences")
        
        // New book-story inspired features
        val ENABLE_BIONIC_READING = booleanPreferencesKey("enable_bionic_reading")
        val ENABLE_HORIZONTAL_LIMITER = booleanPreferencesKey("enable_horizontal_limiter")
        val HORIZONTAL_LIMITER_HEIGHT = floatPreferencesKey("horizontal_limiter_height")
        val HORIZONTAL_LIMITER_OFFSET = floatPreferencesKey("horizontal_limiter_offset")
        val ENABLE_PERCEPTION_EXPANDER = booleanPreferencesKey("enable_perception_expander")
        val PERCEPTION_EXPANDER_PADDING = floatPreferencesKey("perception_expander_padding")
    }
    
    // Font size (14-32)
    val fontSize: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 18f
    }
    
    // Line height multiplier (1.0 - 2.5)
    val lineHeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LINE_HEIGHT] ?: 1.5f
    }
    
    // Letter spacing (-0.05 - 0.3)
    val letterSpacing: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LETTER_SPACING] ?: 0f
    }
    
    // Paragraph spacing (0 - 32)
    val paragraphSpacing: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PARAGRAPH_SPACING] ?: 16f
    }
    
    // Text indent (0 - 48)
    val textIndent: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TEXT_INDENT] ?: 0f
    }
    
    // Enable AI quizzes
    val enableAIQuizzes: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_AI_QUIZZES] ?: true
    }
    
    // Auto download model
    val autoDownloadModel: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DOWNLOAD_MODEL] ?: false
    }
    
    // Speech rate (0.5 - 2.0)
    val speechRate: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SPEECH_RATE] ?: 1.0f
    }
    
    // TTS highlight sentences
    val ttsHighlightSentences: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TTS_HIGHLIGHT_SENTENCES] ?: false
    }
    
    // Theme mode (light, dark, system)
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }
    
    // Reading theme name
    val readingTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[READING_THEME] ?: "Default Light"
    }
    
    // Quiz difficulty (1-5)
    val quizDifficulty: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[QUIZ_DIFFICULTY] ?: 3
    }
    
    // Questions per chapter (1-3)
    val questionsPerChapter: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[QUESTIONS_PER_CHAPTER] ?: 1
    }
    
    // Enable swipe navigation
    val enableSwipeNavigation: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_SWIPE_NAVIGATION] ?: true
    }
    
    // Page turn animation
    val pageTurnAnimation: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PAGE_TURN_ANIMATION] ?: true
    }
    
    // Auto save progress
    val autoSaveProgress: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SAVE_PROGRESS] ?: true
    }
    
    // Reading modes
    val enableReadingRuler: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_READING_RULER] ?: false
    }
    
    val enableRSVPMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_RSVP_MODE] ?: false
    }
    
    val enableParagraphMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_PARAGRAPH_MODE] ?: false
    }
    
    // Book-story inspired features
    val enableBionicReading: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_BIONIC_READING] ?: false
    }
    
    val enableHorizontalLimiter: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_HORIZONTAL_LIMITER] ?: false
    }
    
    val horizontalLimiterHeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[HORIZONTAL_LIMITER_HEIGHT] ?: 100f
    }
    
    val horizontalLimiterOffset: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[HORIZONTAL_LIMITER_OFFSET] ?: 0f
    }
    
    val enablePerceptionExpander: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_PERCEPTION_EXPANDER] ?: false
    }
    
    val perceptionExpanderPadding: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PERCEPTION_EXPANDER_PADDING] ?: 80f
    }
    
    // Update functions
    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size.coerceIn(14f, 32f)
        }
    }
    
    suspend fun setLineHeight(height: Float) {
        context.dataStore.edit { preferences ->
            preferences[LINE_HEIGHT] = height.coerceIn(1.0f, 2.5f)
        }
    }
    
    suspend fun setLetterSpacing(spacing: Float) {
        context.dataStore.edit { preferences ->
            preferences[LETTER_SPACING] = spacing.coerceIn(-0.05f, 0.3f)
        }
    }
    
    suspend fun setParagraphSpacing(spacing: Float) {
        context.dataStore.edit { preferences ->
            preferences[PARAGRAPH_SPACING] = spacing.coerceIn(0f, 32f)
        }
    }
    
    suspend fun setTextIndent(indent: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_INDENT] = indent.coerceIn(0f, 48f)
        }
    }
    
    suspend fun setEnableAIQuizzes(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_AI_QUIZZES] = enable
        }
    }
    
    suspend fun setAutoDownloadModel(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_MODEL] = enable
        }
    }
    
    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPEECH_RATE] = rate.coerceIn(0.5f, 2.0f)
        }
    }
    
    suspend fun setTTSHighlightSentences(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TTS_HIGHLIGHT_SENTENCES] = enable
        }
    }
    
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
    
    suspend fun setReadingTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[READING_THEME] = theme
        }
    }
    
    suspend fun setQuizDifficulty(difficulty: Int) {
        context.dataStore.edit { preferences ->
            preferences[QUIZ_DIFFICULTY] = difficulty.coerceIn(1, 5)
        }
    }
    
    suspend fun setQuestionsPerChapter(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[QUESTIONS_PER_CHAPTER] = count.coerceIn(1, 3)
        }
    }
    
    suspend fun setEnableSwipeNavigation(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_SWIPE_NAVIGATION] = enable
        }
    }
    
    suspend fun setPageTurnAnimation(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PAGE_TURN_ANIMATION] = enable
        }
    }
    
    suspend fun setAutoSaveProgress(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE_PROGRESS] = enable
        }
    }
    
    suspend fun setEnableReadingRuler(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_READING_RULER] = enable
        }
    }
    
    suspend fun setEnableRSVPMode(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_RSVP_MODE] = enable
        }
    }
    
    suspend fun setEnableParagraphMode(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_PARAGRAPH_MODE] = enable
        }
    }
    
    suspend fun setEnableBionicReading(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_BIONIC_READING] = enable
        }
    }
    
    suspend fun setEnableHorizontalLimiter(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_HORIZONTAL_LIMITER] = enable
        }
    }
    
    suspend fun setHorizontalLimiterHeight(height: Float) {
        context.dataStore.edit { preferences ->
            preferences[HORIZONTAL_LIMITER_HEIGHT] = height.coerceIn(50f, 300f)
        }
    }
    
    suspend fun setHorizontalLimiterOffset(offset: Float) {
        context.dataStore.edit { preferences ->
            preferences[HORIZONTAL_LIMITER_OFFSET] = offset.coerceIn(-200f, 200f)
        }
    }
    
    suspend fun setEnablePerceptionExpander(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_PERCEPTION_EXPANDER] = enable
        }
    }
    
    suspend fun setPerceptionExpanderPadding(padding: Float) {
        context.dataStore.edit { preferences ->
            preferences[PERCEPTION_EXPANDER_PADDING] = padding.coerceIn(40f, 200f)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    appSettings: AppSettings = AppSettings(LocalContext.current)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Collect settings
    val fontSize by appSettings.fontSize.collectAsState(initial = 18f)
    val lineHeight by appSettings.lineHeight.collectAsState(initial = 1.5f)
    val letterSpacing by appSettings.letterSpacing.collectAsState(initial = 0f)
    val paragraphSpacing by appSettings.paragraphSpacing.collectAsState(initial = 16f)
    val textIndent by appSettings.textIndent.collectAsState(initial = 0f)
    val enableAIQuizzes by appSettings.enableAIQuizzes.collectAsState(initial = true)
    val autoDownloadModel by appSettings.autoDownloadModel.collectAsState(initial = false)
    val speechRate by appSettings.speechRate.collectAsState(initial = 1.0f)
    val ttsHighlightSentences by appSettings.ttsHighlightSentences.collectAsState(initial = false)
    val themeMode by appSettings.themeMode.collectAsState(initial = "system")
    val readingTheme by appSettings.readingTheme.collectAsState(initial = "Default Light")
    val quizDifficulty by appSettings.quizDifficulty.collectAsState(initial = 3)
    val questionsPerChapter by appSettings.questionsPerChapter.collectAsState(initial = 1)
    val enableSwipeNavigation by appSettings.enableSwipeNavigation.collectAsState(initial = true)
    val pageTurnAnimation by appSettings.pageTurnAnimation.collectAsState(initial = true)
    val autoSaveProgress by appSettings.autoSaveProgress.collectAsState(initial = true)
    val enableReadingRuler by appSettings.enableReadingRuler.collectAsState(initial = false)
    val enableRSVPMode by appSettings.enableRSVPMode.collectAsState(initial = false)
    val enableParagraphMode by appSettings.enableParagraphMode.collectAsState(initial = false)
    val enableBionicReading by appSettings.enableBionicReading.collectAsState(initial = false)
    val enableHorizontalLimiter by appSettings.enableHorizontalLimiter.collectAsState(initial = false)
    val horizontalLimiterHeight by appSettings.horizontalLimiterHeight.collectAsState(initial = 100f)
    val horizontalLimiterOffset by appSettings.horizontalLimiterOffset.collectAsState(initial = 0f)
    val enablePerceptionExpander by appSettings.enablePerceptionExpander.collectAsState(initial = false)
    val perceptionExpanderPadding by appSettings.perceptionExpanderPadding.collectAsState(initial = 80f)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Reading Settings Section
            SettingsSection(title = "Reading") {
                // Font Size
                SettingsSlider(
                    icon = Icons.Default.FormatSize,
                    title = "Font Size",
                    value = fontSize,
                    valueRange = 14f..32f,
                    steps = 17,
                    valueLabel = "${fontSize.toInt()}sp",
                    onValueChange = { scope.launch { appSettings.setFontSize(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Swipe Navigation
                SettingsSwitch(
                    icon = Icons.Default.SwipeRight,
                    title = "Swipe Navigation",
                    subtitle = "Swipe left/right to change chapters",
                    checked = enableSwipeNavigation,
                    onCheckedChange = { scope.launch { appSettings.setEnableSwipeNavigation(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Page Turn Animation
                SettingsSwitch(
                    icon = Icons.Default.Animation,
                    title = "Page Turn Animation",
                    subtitle = "Smooth transitions between pages",
                    checked = pageTurnAnimation,
                    onCheckedChange = { scope.launch { appSettings.setPageTurnAnimation(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Auto Save Progress
                SettingsSwitch(
                    icon = Icons.Default.Save,
                    title = "Auto Save Progress",
                    subtitle = "Automatically bookmark your position",
                    checked = autoSaveProgress,
                    onCheckedChange = { scope.launch { appSettings.setAutoSaveProgress(it) } }
                )
            }
            
            // Typography Settings Section
            SettingsSection(title = "Typography") {
                // Line Height
                SettingsSlider(
                    icon = Icons.Default.LineWeight,
                    title = "Line Height",
                    value = lineHeight,
                    valueRange = 1.0f..2.5f,
                    steps = 14,
                    valueLabel = String.format("%.1fx", lineHeight),
                    onValueChange = { scope.launch { appSettings.setLineHeight(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Letter Spacing
                SettingsSlider(
                    icon = Icons.Default.SpaceBar,
                    title = "Letter Spacing",
                    value = letterSpacing,
                    valueRange = -0.05f..0.3f,
                    steps = 6,
                    valueLabel = String.format("%.2f", letterSpacing),
                    onValueChange = { scope.launch { appSettings.setLetterSpacing(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Paragraph Spacing
                SettingsSlider(
                    icon = Icons.Default.FormatLineSpacing,
                    title = "Paragraph Spacing",
                    value = paragraphSpacing,
                    valueRange = 0f..32f,
                    steps = 15,
                    valueLabel = "${paragraphSpacing.toInt()}dp",
                    onValueChange = { scope.launch { appSettings.setParagraphSpacing(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Text Indent
                SettingsSlider(
                    icon = Icons.Default.FormatIndentIncrease,
                    title = "Text Indent",
                    value = textIndent,
                    valueRange = 0f..48f,
                    steps = 11,
                    valueLabel = "${textIndent.toInt()}dp",
                    onValueChange = { scope.launch { appSettings.setTextIndent(it) } }
                )
            }
            
            // Reading Modes Section
            SettingsSection(title = "Reading Modes") {
                // Reading Ruler
                SettingsSwitch(
                    icon = Icons.Default.HorizontalRule,
                    title = "Reading Ruler",
                    subtitle = "Visual line to track reading position",
                    checked = enableReadingRuler,
                    onCheckedChange = { scope.launch { appSettings.setEnableReadingRuler(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // RSVP Mode
                SettingsSwitch(
                    icon = Icons.Default.Speed,
                    title = "RSVP Speed Reading",
                    subtitle = "Display words one at a time",
                    checked = enableRSVPMode,
                    onCheckedChange = { scope.launch { appSettings.setEnableRSVPMode(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Paragraph Mode
                SettingsSwitch(
                    icon = Icons.Default.FormatAlignLeft,
                    title = "Paragraph Focus Mode",
                    subtitle = "Focus on one paragraph at a time",
                    checked = enableParagraphMode,
                    onCheckedChange = { scope.launch { appSettings.setEnableParagraphMode(it) } }
                )
            }
            
            // Reading Speed Enhancement Section
            SettingsSection(title = "Reading Speed Enhancement") {
                // Bionic Reading
                SettingsSwitch(
                    icon = Icons.Default.TrendingUp,
                    title = "Bionic Reading",
                    subtitle = "Bold first half of words for faster reading",
                    checked = enableBionicReading,
                    onCheckedChange = { scope.launch { appSettings.setEnableBionicReading(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Horizontal Limiter
                SettingsSwitch(
                    icon = Icons.Default.CenterFocusStrong,
                    title = "Focus Zone (Horizontal Limiter)",
                    subtitle = "Dim text outside reading zone",
                    checked = enableHorizontalLimiter,
                    onCheckedChange = { scope.launch { appSettings.setEnableHorizontalLimiter(it) } }
                )
                
                if (enableHorizontalLimiter) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSlider(
                        icon = Icons.Default.Height,
                        title = "Focus Zone Height",
                        value = horizontalLimiterHeight,
                        valueRange = 50f..300f,
                        steps = 24,
                        valueLabel = "${horizontalLimiterHeight.toInt()}dp",
                        onValueChange = { scope.launch { appSettings.setHorizontalLimiterHeight(it) } }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSlider(
                        icon = Icons.Default.VerticalAlignCenter,
                        title = "Focus Zone Offset",
                        value = horizontalLimiterOffset,
                        valueRange = -200f..200f,
                        steps = 39,
                        valueLabel = "${horizontalLimiterOffset.toInt()}dp",
                        onValueChange = { scope.launch { appSettings.setHorizontalLimiterOffset(it) } }
                    )
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Perception Expander
                SettingsSwitch(
                    icon = Icons.Default.UnfoldMore,
                    title = "Perception Expander",
                    subtitle = "Vertical guide lines for eye tracking",
                    checked = enablePerceptionExpander,
                    onCheckedChange = { scope.launch { appSettings.setEnablePerceptionExpander(it) } }
                )
                
                if (enablePerceptionExpander) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsSlider(
                        icon = Icons.Default.SpaceBar,
                        title = "Guide Line Padding",
                        value = perceptionExpanderPadding,
                        valueRange = 40f..200f,
                        steps = 15,
                        valueLabel = "${perceptionExpanderPadding.toInt()}dp",
                        onValueChange = { scope.launch { appSettings.setPerceptionExpanderPadding(it) } }
                    )
                }
            }
            
            // AI Quiz Settings Section
            SettingsSection(title = "AI Comprehension Quizzes") {
                // Enable AI Quizzes
                SettingsSwitch(
                    icon = Icons.Default.Quiz,
                    title = "Enable AI Quizzes",
                    subtitle = "Show quizzes at chapter endings",
                    checked = enableAIQuizzes,
                    onCheckedChange = { scope.launch { appSettings.setEnableAIQuizzes(it) } }
                )
                
                if (enableAIQuizzes) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Auto Download Model
                    SettingsSwitch(
                        icon = Icons.Default.Download,
                        title = "Auto Download AI Model",
                        subtitle = "Download model on WiFi (1.5 GB)",
                        checked = autoDownloadModel,
                        onCheckedChange = { scope.launch { appSettings.setAutoDownloadModel(it) } }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Questions Per Chapter
                    SettingsSlider(
                        icon = Icons.Default.Numbers,
                        title = "Questions Per Chapter",
                        value = questionsPerChapter.toFloat(),
                        valueRange = 1f..3f,
                        steps = 1,
                        valueLabel = "$questionsPerChapter",
                        onValueChange = { scope.launch { appSettings.setQuestionsPerChapter(it.toInt()) } }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Quiz Difficulty
                    SettingsSlider(
                        icon = Icons.Default.TrendingUp,
                        title = "Quiz Difficulty",
                        value = quizDifficulty.toFloat(),
                        valueRange = 1f..5f,
                        steps = 3,
                        valueLabel = when (quizDifficulty) {
                            1 -> "Very Easy"
                            2 -> "Easy"
                            3 -> "Medium"
                            4 -> "Hard"
                            5 -> "Very Hard"
                            else -> "Medium"
                        },
                        onValueChange = { scope.launch { appSettings.setQuizDifficulty(it.toInt()) } }
                    )
                }
            }
            
            // Text-to-Speech Settings
            SettingsSection(title = "Text-to-Speech") {
                SettingsSlider(
                    icon = Icons.Default.Speed,
                    title = "Speech Rate",
                    value = speechRate,
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    valueLabel = String.format("%.1fx", speechRate),
                    onValueChange = { scope.launch { appSettings.setSpeechRate(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsSwitch(
                    icon = Icons.Default.Highlight,
                    title = "Highlight Sentences",
                    subtitle = "Highlight each sentence as it's read",
                    checked = ttsHighlightSentences,
                    onCheckedChange = { scope.launch { appSettings.setTTSHighlightSentences(it) } }
                )
            }
            
            // Appearance Settings
            SettingsSection(title = "Appearance") {
                SettingsRadioGroup(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    options = listOf("Light" to "light", "Dark" to "dark", "System" to "system"),
                    selectedValue = themeMode,
                    onValueChange = { scope.launch { appSettings.setThemeMode(it) } }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Reading Theme Selector
                Text(
                    text = "Reading Theme: $readingTheme",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSlider(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}

@Composable
fun SettingsRadioGroup(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedValue == value,
                    onClick = { onValueChange(value) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label)
            }
        }
    }
}
