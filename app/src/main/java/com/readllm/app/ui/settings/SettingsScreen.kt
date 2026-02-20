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
        val ENABLE_AI_QUIZZES = booleanPreferencesKey("enable_ai_quizzes")
        val AUTO_DOWNLOAD_MODEL = booleanPreferencesKey("auto_download_model")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val QUIZ_DIFFICULTY = intPreferencesKey("quiz_difficulty")
        val QUESTIONS_PER_CHAPTER = intPreferencesKey("questions_per_chapter")
        val ENABLE_SWIPE_NAVIGATION = booleanPreferencesKey("enable_swipe_navigation")
        val PAGE_TURN_ANIMATION = booleanPreferencesKey("page_turn_animation")
        val AUTO_SAVE_PROGRESS = booleanPreferencesKey("auto_save_progress")
    }
    
    // Font size (14-32)
    val fontSize: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 18f
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
    
    // Theme mode (light, dark, system)
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
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
    
    // Update functions
    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size.coerceIn(14f, 32f)
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
    
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
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
    val enableAIQuizzes by appSettings.enableAIQuizzes.collectAsState(initial = true)
    val autoDownloadModel by appSettings.autoDownloadModel.collectAsState(initial = false)
    val speechRate by appSettings.speechRate.collectAsState(initial = 1.0f)
    val themeMode by appSettings.themeMode.collectAsState(initial = "system")
    val quizDifficulty by appSettings.quizDifficulty.collectAsState(initial = 3)
    val questionsPerChapter by appSettings.questionsPerChapter.collectAsState(initial = 1)
    val enableSwipeNavigation by appSettings.enableSwipeNavigation.collectAsState(initial = true)
    val pageTurnAnimation by appSettings.pageTurnAnimation.collectAsState(initial = true)
    val autoSaveProgress by appSettings.autoSaveProgress.collectAsState(initial = true)
    
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
