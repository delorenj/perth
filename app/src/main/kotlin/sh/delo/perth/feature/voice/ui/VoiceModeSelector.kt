package sh.delo.perth.feature.voice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.delo.perth.feature.voice.domain.VoiceMode

/**
 * Horizontal row of three mode selection chips.
 *
 * Exactly one chip is selected at all times. The selected chip is visually highlighted
 * using the primary container colour. Switching modes while recording stops capture first
 * (enforced in [VoiceViewModel.onModeSelected]).
 */
@Composable
fun VoiceModeSelector(
    selectedMode: VoiceMode,
    onModeSelected: (VoiceMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VoiceMode.entries.forEach { mode ->
            ModeChip(
                mode = mode,
                selected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ModeChip(
    mode: VoiceMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = mode.label,
                fontSize = 12.sp,
                maxLines = 1,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = modifier,
    )
}

private val VoiceMode.label: String
    get() = when (this) {
        VoiceMode.Transcription -> "Transcription"
        VoiceMode.Task -> "Task"
        VoiceMode.Command -> "Command"
    }
