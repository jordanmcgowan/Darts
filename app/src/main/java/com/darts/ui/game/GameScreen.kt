package com.darts.ui.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.hilt.navigation.compose.hiltViewModel
import com.darts.MainViewModel
import com.darts.MainViewState
import com.darts.data.DartsPlayer
import com.darts.data.GameVariation

@Composable
fun GameScreenWithoutInfo(
  navigateToHistory: () -> Unit,
  navigateToHome: () -> Unit,
  viewModel: MainViewModel = hiltViewModel()
) {
  LaunchedEffect(viewModel) {
    viewModel.getMostRecentGame()
  }

  val viewState: MainViewState by viewModel.viewState.collectAsState()

  GameScreen(
    viewState = viewState,
    navigateToHistory = navigateToHistory,
    navigateToHome = navigateToHome
  )
}

@Composable
fun GameScreenWithTimestamp(
  gameTimestamp: Long,
  navigateToHistory: () -> Unit,
  navigateToHome: () -> Unit,
  viewModel: MainViewModel = hiltViewModel()
) {
  LaunchedEffect(viewModel) {
    viewModel.getGame(gameTimestamp)
  }

  val viewState: MainViewState by viewModel.viewState.collectAsState()

  GameScreen(
    viewState = viewState,
    navigateToHistory = navigateToHistory,
    navigateToHome = navigateToHome
  )
}

@Composable
fun GameScreen(
  viewState: MainViewState,
  navigateToHistory: () -> Unit,
  navigateToHome: () -> Unit,
) {
  when (viewState) {
    MainViewState.Loading -> {
      CircularProgressIndicator()
    }
    MainViewState.Failure -> {
      AlertDialog(
        onDismissRequest = { navigateToHome() },
        title = { Text(text = "Sorry, there was an issue") },
        text = { Text(text = "Start a new game or try again later")},
        confirmButton = { Button(onClick = { navigateToHome() }, content = { Text(text = "Start new game") }) }
      )
    }
    is MainViewState.Content -> {
      when (viewState.game.gameVariation) {
        GameVariation.CRICKET -> {
          CricketGame(
            viewState.game,
            navigateToHistory
          )
        }
        GameVariation.OH_ONE -> {
          OhOneGame(
            viewState.game,
            navigateToHistory
          )
        }
      }
    }
  }
}

@Composable
fun DartsText(
  value: String,
  shouldStrikethrough: Boolean = false,
  style: TextStyle? = null
) {

  val officialStyle = style ?: MaterialTheme.typography.bodyMedium
  val updatedStyle = officialStyle.copy(textDecoration = if(shouldStrikethrough) TextDecoration.LineThrough else null)

  Text(
    text = value,
    style = updatedStyle
  )
}

@Composable
fun DartsWinner(
  navigateToHistory: () -> Unit,
  winner: DartsPlayer
) {
  AlertDialog(
    onDismissRequest = { navigateToHistory() },
    title = { Text(text = "GAME OVER") },
    text = { Text(text = "${winner.name} is the winner! Congratulations") },
    confirmButton = {
      TextButton(onClick = { navigateToHistory() }) {
        Text(text = "Show game logs")
      }
    }
  )
}

enum class DartBoardMark(val displayName: String, val pointValue: Int) {
  BULLSEYE("BULL", 25),
  TWENTY("20", 20),
  NINETEEN("19", 19),
  EIGHTEEN("18", 18),
  SEVENTEEN("17", 17),
  SIXTEEN("16", 16),
  FIFTEEN("15", 15),
  FOURTEEN("14", 14),
  THIRTEEN("13", 13),
  TWELVE("12", 12),
  ELEVEN("11", 11),
  TEN("10", 10),
  NINE("9", 9),
  EIGHT("8", 8),
  SEVEN("7", 7),
  SIX("6", 6),
  FIVE("5", 5),
  FOUR("4", 4),
  THREE("3", 3),
  TWO("2", 2),
  ONE("1", 1);


  companion object {
    val cricketMarks = listOf(TWENTY, NINETEEN, EIGHTEEN, SEVENTEEN, SIXTEEN, FIFTEEN, BULLSEYE)
    // BULLSEYE is left off this list as it's manually added when choosing "random" cricket
    val fullMarks = listOf(TWENTY, NINETEEN, EIGHTEEN, SEVENTEEN, SIXTEEN, FIFTEEN, FOURTEEN, THIRTEEN, TWELVE, ELEVEN, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO, ONE)
  }
}
