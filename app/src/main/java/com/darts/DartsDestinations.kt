package com.darts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface DartsDestination {
  val icon: ImageVector
  val route: String
}

/**
 * Darts navigation destinations
 */
object Home: DartsDestination {
  override val icon = Icons.Filled.Home
  override val route = "overview"
}

object CurrentGame: DartsDestination {
  override val icon = Icons.Filled.PlayArrow
  override val route = "game"
  const val gameIdArgument: String = "gameId"
  val routeWithArgs = "$route/{$gameIdArgument}"
  val arguments = listOf(navArgument(gameIdArgument) { type = NavType.LongType })
}

object History: DartsDestination {
  override val icon = Icons.Filled.DateRange
  override val route = "history"
}


// Screens to be displayed in the top NavBar
val dartsTabRowScreens = listOf(Home, CurrentGame, History)
