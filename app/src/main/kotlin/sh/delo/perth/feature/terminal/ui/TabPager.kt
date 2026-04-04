package sh.delo.perth.feature.terminal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ZellijTab

/**
 * HorizontalPager-based tab container for Story 2.1.
 *
 * Swipe left/right navigates between tabs. Tapping a tab name in the indicator bar also
 * navigates directly. Both directions keep [selectedTabIndex]/[onTabSelected] in sync so
 * the ViewModel remains the single source of truth.
 */
@Composable
fun TabPager(
    tabs: List<ZellijTab>,
    selectedTabIndex: Int,
    activePaneId: PaneId?,
    paneOutput: Map<String, List<PaneOutput>>,
    onTabSelected: (Int) -> Unit,
    onPaneSelected: (PaneId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tabs.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex.coerceIn(0, tabs.lastIndex),
        pageCount = { tabs.size },
    )

    // Sync swipe -> ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                if (page != selectedTabIndex) {
                    onTabSelected(page)
                }
            }
    }

    // Sync ViewModel -> pager (tap on tab chip)
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabIndicatorBar(
            tabs = tabs,
            pagerState = pagerState,
            onTabSelected = { index ->
                onTabSelected(index)
            },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { index -> tabs[index].id },
        ) { page ->
            val tab = tabs[page]
            PaneGrid(
                tab = tab,
                activePaneId = activePaneId,
                paneOutput = paneOutput,
                onPaneSelected = onPaneSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TabIndicatorBar(
    tabs: List<ZellijTab>,
    pagerState: PagerState,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = modifier.fillMaxWidth(),
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {},  // custom indicator handled by FilterChip selection styling
        divider = {},
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == pagerState.currentPage
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTabSelected(index) },
                            label = {
                                Text(
                                    text = tab.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                },
            )
        }
    }
}
