package sh.delo.perth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import sh.delo.perth.navigation.PerthNavHost
import sh.delo.perth.service.PerthForegroundService
import sh.delo.perth.ui.theme.PerthTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // The foreground service anchors WebSocket + microphone lifetime so neither
        // is killed under memory pressure mid-session. Started here for v0.1; a
        // future refactor will gate this on actual session/recording state.
        PerthForegroundService.start(this)
        setContent {
            PerthTheme {
                PerthNavHost()
            }
        }
    }

    override fun onDestroy() {
        PerthForegroundService.stop(this)
        super.onDestroy()
    }
}
