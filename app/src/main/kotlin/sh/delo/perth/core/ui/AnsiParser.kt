package sh.delo.perth.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * Basic ANSI escape sequence parser for Story 5.5.
 * Converts terminal escape codes (SGR) into Compose [AnnotatedString] styles.
 *
 * Supported:
 * - 30-37 / 90-97 (Foreground colors)
 * - 40-47 / 100-107 (Background colors)
 * - 1 (Bold)
 * - 4 (Underline)
 * - 0 (Reset)
 */
object AnsiParser {
    private val ANSI_REGEX = Regex("\u001B\\[([0-9;]*)m")

    fun parse(text: String): AnnotatedString = buildAnnotatedString {
        var lastIndex = 0
        var currentStyle = SpanStyle()

        ANSI_REGEX.findAll(text).forEach { match ->
            // Append plain text before the match
            val plainText = text.substring(lastIndex, match.range.first)
            if (plainText.isNotEmpty()) {
                withStyle(currentStyle) {
                    append(plainText)
                }
            }

            // Update style based on the sequence
            val codes = match.groupValues[1].split(';').mapNotNull { it.toIntOrNull() }
            currentStyle = updateStyle(currentStyle, codes)

            lastIndex = match.range.last + 1
        }

        // Append remaining text
        if (lastIndex < text.length) {
            withStyle(currentStyle) {
                append(text.substring(lastIndex))
            }
        }
    }

    private fun updateStyle(current: SpanStyle, codes: List<Int>): SpanStyle {
        var next = if (codes.contains(0) || codes.isEmpty()) SpanStyle() else current

        codes.forEach { code ->
            when (code) {
                0 -> { /* Already handled by reset above */ }
                1 -> next = next.copy(fontWeight = FontWeight.Bold)
                4 -> next = next.copy(textDecoration = TextDecoration.Underline)
                in 30..37 -> next = next.copy(color = mapStandardColor(code - 30, bright = false))
                in 90..97 -> next = next.copy(color = mapStandardColor(code - 90, bright = true))
                in 40..47 -> next = next.copy(background = mapStandardColor(code - 40, bright = false))
                in 100..107 -> next = next.copy(background = mapStandardColor(code - 100, bright = true))
                // Handle 38/48 (Extended 256-color or RGB) if needed in future
            }
        }
        return next
    }

    private fun mapStandardColor(index: Int, bright: Boolean): Color = when (index) {
        0 -> if (bright) Color(0xFF555555) else Color(0xFF000000) // Black
        1 -> if (bright) Color(0xFFFF5555) else Color(0xFFBB0000) // Red
        2 -> if (bright) Color(0xFF55FF55) else Color(0xFF00BB00) // Green
        3 -> if (bright) Color(0xFFFFFF55) else Color(0xFFBBBB00) // Yellow
        4 -> if (bright) Color(0xFF5555FF) else Color(0xFF0000BB) // Blue
        5 -> if (bright) Color(0xFFFF55FF) else Color(0xFFBB00BB) // Magenta
        6 -> if (bright) Color(0xFF55FFFF) else Color(0xFF00BBBB) // Cyan
        7 -> if (bright) Color(0xFFFFFFFF) else Color(0xFFBBBBBB) // White
        else -> Color.Unspecified
    }
}
