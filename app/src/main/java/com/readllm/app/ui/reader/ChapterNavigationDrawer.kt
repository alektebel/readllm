package com.readllm.app.ui.reader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.readllm.app.reader.EpubReaderService

/**
 * Data model for expandable chapter hierarchy
 */
data class ExpandableChapter(
    val chapter: EpubReaderService.Chapter,
    val isExpanded: Boolean = false,
    val hasChildren: Boolean = false,
    val isNested: Boolean = false,
    val children: List<EpubReaderService.Chapter> = emptyList()
)

/**
 * Chapter Navigation Drawer
 * 
 * Displays a table of contents with expandable/collapsible nested chapters.
 * Auto-expands parent chapters when current chapter is a child.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterNavigationDrawer(
    chapters: List<EpubReaderService.Chapter>,
    currentChapterIndex: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Group chapters into expandable hierarchy
    val expandableChapters = remember(chapters, currentChapterIndex) {
        groupChaptersIntoHierarchy(chapters, currentChapterIndex)
    }
    
    var expandedStates by remember {
        mutableStateOf(expandableChapters.associate { it.chapter.order to it.isExpanded })
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "Table of Contents",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Divider()
            
            // Chapter list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                expandableChapters.forEach { expandable ->
                    // Parent chapter item
                    item(key = "parent_${expandable.chapter.order}") {
                        ChapterItem(
                            chapter = expandable.chapter,
                            isNested = false,
                            isCurrent = currentChapterIndex == expandable.chapter.order,
                            isExpanded = expandedStates[expandable.chapter.order] ?: false,
                            hasChildren = expandable.hasChildren,
                            onClick = {
                                onChapterSelected(expandable.chapter.order)
                                onDismiss()
                            },
                            onExpandToggle = if (expandable.hasChildren) {
                                {
                                    expandedStates = expandedStates.toMutableMap().apply {
                                        put(expandable.chapter.order, !(this[expandable.chapter.order] ?: false))
                                    }
                                }
                            } else null
                        )
                    }
                    
                    // Child chapters (if expanded)
                    if (expandedStates[expandable.chapter.order] == true && expandable.children.isNotEmpty()) {
                        items(
                            items = expandable.children,
                            key = { "child_${it.order}" }
                        ) { childChapter ->
                            ChapterItem(
                                chapter = childChapter,
                                isNested = true,
                                isCurrent = currentChapterIndex == childChapter.order,
                                isExpanded = false,
                                hasChildren = false,
                                onClick = {
                                    onChapterSelected(childChapter.order)
                                    onDismiss()
                                },
                                onExpandToggle = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: EpubReaderService.Chapter,
    isNested: Boolean,
    isCurrent: Boolean,
    isExpanded: Boolean,
    hasChildren: Boolean,
    onClick: () -> Unit,
    onExpandToggle: (() -> Unit)?
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand_rotation"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = if (isNested) 32.dp else 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chapter title
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Expand/collapse icon (only for parent chapters with children)
        if (hasChildren && onExpandToggle != null) {
            IconButton(
                onClick = { 
                    onExpandToggle()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

/**
 * Groups chapters into hierarchy by detecting nested chapters.
 * Nested chapters are identified by indentation patterns in titles or numbering (e.g., 1.1, 1.2).
 */
private fun groupChaptersIntoHierarchy(
    chapters: List<EpubReaderService.Chapter>,
    currentChapterIndex: Int
): List<ExpandableChapter> {
    val grouped = mutableListOf<ExpandableChapter>()
    var i = 0
    
    while (i < chapters.size) {
        val current = chapters[i]
        val children = mutableListOf<EpubReaderService.Chapter>()
        
        // Look ahead for nested chapters
        // Heuristic: Next chapter is nested if:
        // 1. Title has more dots/numbers (e.g., "1.1" after "1")
        // 2. Title is indented or prefixed with special chars
        var j = i + 1
        while (j < chapters.size) {
            val next = chapters[j]
            if (isNestedChapter(current.title, next.title)) {
                children.add(next)
                j++
            } else {
                break
            }
        }
        
        // Check if current chapter is in children - if so, auto-expand
        val shouldExpand = children.any { it.order == currentChapterIndex }
        
        grouped.add(
            ExpandableChapter(
                chapter = current,
                isExpanded = shouldExpand,
                hasChildren = children.isNotEmpty(),
                isNested = false,
                children = children
            )
        )
        
        i = j
    }
    
    return grouped
}

/**
 * Detects if childTitle is a nested chapter under parentTitle
 */
private fun isNestedChapter(parentTitle: String, childTitle: String): Boolean {
    // Pattern 1: Numbering like "1" -> "1.1", "1.2"
    val parentNumber = extractLeadingNumber(parentTitle)
    val childNumber = extractLeadingNumber(childTitle)
    
    if (parentNumber != null && childNumber != null) {
        // Check if child starts with parent number followed by a dot
        if (childNumber.startsWith("$parentNumber.") && !childNumber.substring(parentNumber.length + 1).contains(".")) {
            return true
        }
    }
    
    // Pattern 2: Indentation markers (common in some EPUBs)
    if (childTitle.trimStart().length < childTitle.length && 
        parentTitle.trimStart().length == parentTitle.length) {
        return true
    }
    
    // Pattern 3: Special prefix markers like "→", "•", "-", "*"
    val childTrimmed = childTitle.trim()
    if (childTrimmed.startsWith("→") || 
        childTrimmed.startsWith("•") || 
        childTrimmed.startsWith("-") || 
        childTrimmed.startsWith("*")) {
        return true
    }
    
    return false
}

/**
 * Extracts leading number from chapter title (e.g., "1.2.3" from "1.2.3 Introduction")
 */
private fun extractLeadingNumber(title: String): String? {
    val trimmed = title.trim()
    val match = Regex("^(\\d+(?:\\.\\d+)*)").find(trimmed)
    return match?.groupValues?.get(1)
}
