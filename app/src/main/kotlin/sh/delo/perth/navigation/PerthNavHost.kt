package sh.delo.perth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import sh.delo.perth.feature.session.ui.SessionListScreen
import sh.delo.perth.feature.settings.ui.SettingsScreen
import sh.delo.perth.feature.terminal.ui.TerminalScreen

@Composable
fun PerthNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.SessionList,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Route.SessionList> {
            SessionListScreen(
                onNavigateToTerminal = { sessionId ->
                    navController.navigate(Route.Terminal(sessionId = sessionId))
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                },
            )
        }

        composable<Route.Terminal> { backStackEntry ->
            val route: Route.Terminal = backStackEntry.toRoute()
            TerminalScreen(
                sessionId = route.sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSessions = {
                    // Pop Settings off the back stack and go to SessionList on successful connect
                    navController.navigate(Route.SessionList) {
                        popUpTo(Route.Settings) { inclusive = true }
                    }
                },
            )
        }
    }
}
