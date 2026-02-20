package com.readllm.app.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Quiz screen for displaying comprehension questions
 * Redesigned for better UX with cleaner, less overwhelming interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    question: ComprehensionQuizService.QuizQuestion,
    chapterContent: String,
    quizService: ComprehensionQuizService,
    onAnswerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var userTypedAnswer by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var answerJudgment by remember { mutableStateOf<ComprehensionQuizService.AnswerJudgment?>(null) }
    var isEvaluating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Minimalist header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quick Check",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Question card with better visual hierarchy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 28.sp
                )
            }
            
            // Answer input with better spacing
            if (question.questionType == ComprehensionQuizService.QuestionFormat.FILL_IN) {
                OutlinedTextField(
                    value = userTypedAnswer,
                    onValueChange = { userTypedAnswer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    label = { Text("Your Answer") },
                    placeholder = { Text("Share your thoughts...") },
                    enabled = !showResult,
                    minLines = 2,
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = MaterialTheme.shapes.medium
                )
            } else {
                // Legacy multiple choice
                question.acceptableAnswers.forEach { option ->
                    AnswerOption(
                        text = option,
                        isSelected = selectedAnswer == option,
                        showResult = showResult,
                        isCorrect = option == question.correctAnswer,
                        onClick = {
                            if (!showResult) {
                                selectedAnswer = option
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Large, prominent action button
            Button(
                onClick = {
                    if (!showResult) {
                        val answer = if (question.questionType == ComprehensionQuizService.QuestionFormat.FILL_IN) {
                            userTypedAnswer
                        } else {
                            selectedAnswer ?: ""
                        }
                        
                        if (answer.isNotEmpty()) {
                            isEvaluating = true
                            coroutineScope.launch {
                                answerJudgment = quizService.judgeAnswer(answer, question, chapterContent)
                                showResult = true
                                isEvaluating = false
                                onAnswerSelected(answer)
                            }
                        }
                    } else {
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = if (isEvaluating) {
                    false
                } else if (question.questionType == ComprehensionQuizService.QuestionFormat.FILL_IN) {
                    userTypedAnswer.isNotBlank()
                } else {
                    selectedAnswer != null
                },
                shape = MaterialTheme.shapes.medium
            ) {
                if (isEvaluating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Evaluating...",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        text = if (!showResult) "Submit" else "Continue Reading",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Feedback card with improved design
            if (showResult && answerJudgment != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            answerJudgment!!.isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                            answerJudgment!!.score >= 50 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (answerJudgment!!.isCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = when {
                                    answerJudgment!!.isCorrect -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when {
                                    answerJudgment!!.score == 100 -> "Perfect! ✨"
                                    answerJudgment!!.score >= 90 -> "Great!"
                                    answerJudgment!!.score >= 70 -> "Good!"
                                    answerJudgment!!.score >= 50 -> "Close"
                                    else -> "Keep trying"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = when {
                                    answerJudgment!!.isCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                        
                        Text(
                            text = answerJudgment!!.feedback,
                            style = MaterialTheme.typography.bodyLarge,
                            color = when {
                                answerJudgment!!.isCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    showResult: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showResult && isCorrect -> MaterialTheme.colorScheme.primaryContainer
        showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        showResult && isCorrect -> MaterialTheme.colorScheme.primary
        showResult && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor),
            width = if (isSelected || (showResult && isCorrect)) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (showResult && isCorrect) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Correct",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (showResult && isSelected && !isCorrect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Incorrect",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Quiz results summary dialog - Redesigned for better UX
 */
@Composable
fun QuizResultsDialog(
    correctAnswers: Int,
    totalQuestions: Int,
    onDismiss: () -> Unit
) {
    val percentage = (correctAnswers.toFloat() / totalQuestions * 100).toInt()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chapter Complete!",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Score display
                Text(
                    text = "$correctAnswers / $totalQuestions",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "questions correct",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = correctAnswers.toFloat() / totalQuestions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$percentage% comprehension",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Encouraging message
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            percentage >= 90 -> MaterialTheme.colorScheme.tertiaryContainer
                            percentage >= 70 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when {
                            percentage >= 90 -> "Excellent! You have a strong grasp of this material. Keep up the great work!"
                            percentage >= 70 -> "Well done! You understood the key concepts. Continue reading!"
                            percentage >= 50 -> "Good effort! Consider reviewing this chapter to strengthen your understanding."
                            else -> "Take your time with the material. Re-reading can help solidify your comprehension."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        color = when {
                            percentage >= 90 -> MaterialTheme.colorScheme.onTertiaryContainer
                            percentage >= 70 -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Continue Reading")
            }
        }
    )
}
