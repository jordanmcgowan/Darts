package com.darts.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.darts.CurrentGame
import com.darts.History
import com.darts.Home
import com.darts.ui.game.GameScreenWithTimestamp
import com.darts.ui.game.GameScreenWithoutInfo
import com.darts.ui.history.HistoryScreen
import com.darts.ui.home.HomeScreen

// TODO = convert to use this
@Composable
fun DartsNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = Home.route,
    modifier = modifier
  ) {
    composable(route = Home.route) {
      HomeScreen(
        startGame = { gameTimestamp -> navController.navigateSingleTopTo("game/${gameTimestamp}") }
      )
    }
    composable(route = CurrentGame.routeWithArgs, arguments = CurrentGame.arguments) { navBackStackEntry ->
      /* Extracting the id from the route */
      val gameTimestamp = navBackStackEntry.arguments?.getLong(CurrentGame.gameIdArgument)
      /* We check if is null */
      gameTimestamp?.let {
        GameScreenWithTimestamp(
          gameTimestamp = gameTimestamp,
          navigateToHome = { navController.navigateSingleTopTo(Home.route) },
          navigateToHistory = { navController.navigateSingleTopTo(History.route) }
        )
      }
    }
    composable(route = CurrentGame.route) {
      GameScreenWithoutInfo(
        navigateToHome = { navController.navigateSingleTopTo(Home.route) },
        navigateToHistory = { navController.navigateSingleTopTo(History.route) }
      )
    }
    composable(route = History.route) {
      HistoryScreen(
        navigateToGame = { gameTimestamp -> navController.navigateSingleTopTo("game/${gameTimestamp}") }
      )
    }
  }
}

fun NavHostController.navigateSingleTopTo(route: String) =
  this.navigate(route) {
    launchSingleTop = true
    restoreState = true
  }