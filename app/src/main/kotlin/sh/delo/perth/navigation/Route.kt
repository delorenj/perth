package sh.delo.perth.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes for Perth. */
sealed interface Route {

    @Serializable
    data object SessionList : Route

    @Serializable
    data class Terminal(val sessionId: String) : Route

    @Serializable
    data object Settings : Route
}
