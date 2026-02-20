package com.readllm.app.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.readllm.app.model.ChapterScoreEntity

/**
 * Comprehension analytics dashboard
 * Shows reading progress, quiz scores, and insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensionDashboard(
    bookTitle: String,
    chapterScores: List<ChapterScoreEntity>,
    onClose: () -> Unit
) {
    val totalQuestions = chapterScores.sumOf { it.questionsAsked }
    val correctAnswers = chapterScores.sumOf { it.correctAnswers }
    val averageScore = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions * 100).toInt()
    } else 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprehension Analytics") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book title
            item {
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Overall stats card
            item {
                OverallStatsCard(
                    averageScore = averageScore,
                    totalChapters = chapterScores.size,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers
                )
            }
            
            // Performance chart
            item {
                PerformanceChartCard(chapterScores)
            }
            
            // Chapter breakdown
            item {
                Text(
                    text = "Chapter Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(chapterScores.sortedByDescending { it.timestamp }) { score ->
                ChapterScoreCard(score)
            }
            
            // Insights
            item {
                InsightsCard(chapterScores, averageScore)
            }
        }
    }
}

@Composable
fun OverallStatsCard(
    averageScore: Int,
    totalChapters: Int,
    totalQuestions: Int,
    correctAnswers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Overall Comprehension",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "$averageScore%",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = averageScore / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Chapters",
                    value = totalChapters.toString(),
                    icon = Icons.Default.Book
                )
                StatItem(
                    label = "Questions",
                    value = totalQuestions.toString(),
                    icon = Icons.Default.Quiz
                )
                StatItem(
                    label = "Correct",
                    value = correctAnswers.toString(),
                    icon = Icons.Default.Check
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun PerformanceChartCard(chapterScores: List<ChapterScoreEntity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Trend",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (chapterScores.isEmpty()) {
                Text(
                    text = "No quiz data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Simple bar chart representation
                chapterScores.sortedBy { it.chapterId }.forEach { score ->
                    val percentage = if (score.questionsAsked > 0) {
                        (score.correctAnswers.toFloat() / score.questionsAsked * 100).toInt()
                    } else 0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ch ${score.chapterId}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(48.dp)
                        )
                        
                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .padding(horizontal = 8.dp)
                        )
                        
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterScoreCard(score: ChapterScoreEntity) {
    val percentage = if (score.questionsAsked > 0) {
        (score.correctAnswers.toFloat() / score.questionsAsked * 100).toInt()
    } else 0
    
    val color = when {
        percentage >= 90 -> MaterialTheme.colorScheme.primaryContainer
        percentage >= 70 -> MaterialTheme.colorScheme.secondaryContainer
        percentage >= 50 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = color
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chapter ${score.chapterId}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${score.correctAnswers} / ${score.questionsAsked} correct",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Score: ${score.scorePercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InsightsCard(chapterScores: List<ChapterScoreEntity>, averageScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insights & Recommendations",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                chapterScores.isEmpty() -> {
                    InsightItem("Start reading and take quizzes to see personalized insights!")
                }
                averageScore >= 90 -> {
                    InsightItem("Excellent comprehension! You're retaining information very well.")
                    InsightItem("Keep up the great work and maintain this pace.")
                }
                averageScore >= 70 -> {
                    InsightItem("Good progress! You're understanding most concepts.")
                    val weakChapters = chapterScores.filter { 
                        (it.correctAnswers.toFloat() / it.questionsAsked * 100) < 70 
                    }
                    if (weakChapters.isNotEmpty()) {
                        InsightItem("Consider reviewing chapters: ${weakChapters.joinToString { it.chapterId.toString() }}")
                    }
                }
                averageScore >= 50 -> {
                    InsightItem("You're making progress, but might benefit from slower reading.")
                    InsightItem("Try taking notes or highlighting key passages.")
                }
                else -> {
                    InsightItem("Consider re-reading chapters with low scores.")
                    InsightItem("Take breaks between sections to improve retention.")
                }
            }
            
            // Trend analysis
            if (chapterScores.size >= 3) {
                val recentScores = chapterScores.sortedByDescending { it.timestamp }.take(3)
                val recentAvg = recentScores.sumOf { 
                    (it.correctAnswers.toFloat() / it.questionsAsked * 100).toInt() 
                } / 3
                
                val olderScores = chapterScores.sortedByDescending { it.timestamp }
                    .drop(3).take(3)
                if (olderScores.isNotEmpty()) {
                    val olderAvg = olderScores.sumOf { 
                        (it.correctAnswers.toFloat() / it.questionsAsked * 100).toInt() 
                    } / olderScores.size
                    
                    when {
                        recentAvg > olderAvg + 10 -> InsightItem("📈 Your comprehension is improving!")
                        recentAvg < olderAvg - 10 -> InsightItem("📉 Recent scores are lower. Consider taking breaks.")
                        else -> InsightItem("📊 Your performance is consistent.")
                    }
                }
            }
        }
    }
}

@Composable
fun InsightItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
