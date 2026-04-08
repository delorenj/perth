package sh.delo.perth.feature.terminal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ZellijPane
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.ui.AnsiParser

private val PANE_CORNER = RoundedCornerShape(4.dp)

/**
 * Renders all panes for a single [ZellijTab] in a vertical stack.
 *
 * For Story 2.2 (MVP): single-column layout where each pane is displayed as a full-width
 * block. The active pane is highlighted with a primary-colour border. Tapping any pane
 * promotes it to the active state via [onPaneSelected].
 */
@Composable
fun PaneGrid(
    tab: ZellijTab,
    activePaneId: PaneId?,
    paneOutput: Map<String, List<PaneOutput>>,
    onPaneSelected: (PaneId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        tab.panes.forEachIndexed { index, pane ->
            val isActive = pane.id == activePaneId
            val outputs = paneOutput[pane.id.value] ?: emptyList()

            PaneView(
                pane = pane,
                outputs = outputs,
                isActive = isActive,
                onSelected = { onPaneSelected(pane.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            if (index < tab.panes.lastIndex) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

/**
 * A single pane view: monospace terminal output with an active-pane border/highlight.
 *
 * Terminal output auto-scrolls to the bottom whenever new lines arrive.
 */
@Composable
fun PaneView(
    pane: ZellijPane,
    outputs: List<PaneOutput>,
    isActive: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new output
    LaunchedEffect(outputs.size) {
        if (outputs.isNotEmpty()) {
            listState.animateScrollToItem(outputs.size - 1)
        }
    }

    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isActive) 2.dp else 1.dp

    Box(
        modifier = modifier
            .clip(PANE_CORNER)
            .border(BorderStroke(borderWidth, borderColor), PANE_CORNER)
            .background(
                if (isActive) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface,
            )
            .clickable(
                role = Role.Button,
                onClickLabel = "Select pane ${pane.title.ifBlank { pane.id.value }}",
                onClick = onSelected,
            ),
    ) {
        // Optional pane title header
        if (pane.title.isNotBlank()) {
            Text(
                text = pane.title,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                maxLines = 1,
            )
        }

        val topPadding = if (pane.title.isNotBlank()) 18.dp else 4.dp

        if (outputs.isEmpty()) {
            Text(
                text = "No output yet...",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = topPadding, end = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = topPadding,
                    bottom = 4.dp,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(outputs, key = { it.timestamp.toEpochMilli() }) { output ->
                    Text(
                        text = AnsiParser.parse(output.text),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                        ),
                        fontFamily = FontFamily.Monospace,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
