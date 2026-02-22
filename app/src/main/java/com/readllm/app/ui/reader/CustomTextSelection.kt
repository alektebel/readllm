package com.readllm.app.ui.reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Custom Text Selection Toolbar
 * 
 * Provides enhanced text selection with:
 * - Copy to clipboard
 * - Share selected text
 * - Web search
 * - Translate
 * - Dictionary lookup
 */
object CustomTextSelection {
    
    /**
     * Copies text to clipboard
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Selected text", text)
        clipboard.setPrimaryClip(clip)
    }
    
    /**
     * Shares text via Android share sheet
     */
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share text"))
    }
    
    /**
     * Opens web search for selected text
     */
    fun webSearch(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", text)
        }
        
        // Fallback to browser if no search app
        if (intent.resolveActivity(context.packageManager) == null) {
            val searchUrl = "https://www.google.com/search?q=${Uri.encode(text)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
            context.startActivity(browserIntent)
        } else {
            context.startActivity(intent)
        }
    }
    
    /**
     * Opens translator for selected text
     * Uses ACTION_PROCESS_TEXT if available, otherwise falls back to web search
     */
    fun translate(context: Context, text: String) {
        // Try Android's ACTION_PROCESS_TEXT (Android 6.0+)
        val processIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            type = "text/plain"
        }
        
        if (processIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(processIntent, "Translate"))
        } else {
            // Fallback: Web search with "translate:" prefix
            val searchUrl = "https://translate.google.com/?text=${Uri.encode(text)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
            context.startActivity(browserIntent)
        }
    }
    
    /**
     * Opens dictionary for selected text
     * Uses ACTION_PROCESS_TEXT if available, otherwise uses OneLook dictionary
     */
    fun dictionary(context: Context, text: String) {
        // Try Android's ACTION_PROCESS_TEXT (Android 6.0+)
        val processIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            type = "text/plain"
        }
        
        if (processIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(processIntent, "Define"))
        } else {
            // Fallback: OneLook dictionary website
            val dictUrl = "https://www.onelook.com/?w=${Uri.encode(text)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(dictUrl))
            context.startActivity(browserIntent)
        }
    }
}

/**
 * Enhanced selection container with custom toolbar
 * 
 * Note: Android's SelectionContainer doesn't allow easy toolbar customization.
 * This provides utility functions that can be called from a custom UI.
 * 
 * For full custom toolbar, would need to implement custom text selection from scratch
 * using BasicTextField with custom TextToolbar, which is complex.
 * 
 * Current approach: Provide utility functions accessible via dialog/menu
 */
@Composable
fun EnhancedSelectionContainer(
    content: @Composable () -> Unit
) {
    SelectionContainer {
        content()
    }
}
