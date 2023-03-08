package com.darts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.darts.ui.components.NavBar
import com.darts.ui.game.GameScreenWithTimestamp
import com.darts.ui.game.GameScreenWithoutInfo
import com.darts.ui.history.HistoryScreen
import com.darts.ui.home.HomeScreen
import com.darts.ui.theme.DartsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DartsApp()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DartsApp() {
  DartsTheme {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    // Fetch your currentDestination:
    val currentDestination = currentBackStack?.destination

    val currentScreen = dartsTabRowScreens.find { it.route == currentDestination?.route } ?: Home

    Scaffold(
      topBar = {
        NavBar(
          allScreens = dartsTabRowScreens,
          onTabSelected = { newScreen -> navController.navigateSingleTopTo(newScreen.route) },
          currentScreen = currentScreen
        )
      },
      content = {
        NavHost(
          navController = navController,
          startDestination = Home.route,
          modifier = Modifier.padding(it)
        ) {
          composable(route = Home.route) {
            HomeScreen(
              startGame = { gameTimestamp -> navController.navigateSingleTopTo("game/${gameTimestamp}") }
            )
          }
          composable(route = CurrentGame.routeWithArgs, arguments = CurrentGame.arguments) {navBackStackEntry ->
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
              navigateToGame = { gameTimestamp -> navController.navigateSingleTopTo("game/${gameTimestamp}")  }
            )
          }
        }
      }
    )
  }
}

fun NavHostController.navigateSingleTopTo(route: String) =
  this.navigate(route) {
    launchSingleTop = true
    restoreState = true
  }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  DartsApp()
}