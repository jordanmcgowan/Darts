package com.darts.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.darts.data.DartsGame
import com.darts.data.GameScore
import com.darts.data.OhOnePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun OhOneGame(
  currentGame: DartsGame,
  navigateToHistory: () -> Unit,
  viewModel: OhOneViewModel = hiltViewModel()
) {

  val gameState: GameStatus by viewModel.gameState.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  OhOneGame(
    snackbarState = snackbarHostState,
    currentGame = currentGame,
    scope = scope,
    viewModel = viewModel,
    navigateToHistory = navigateToHistory,
    gameStatus = gameState
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhOneGame(
  snackbarState: SnackbarHostState,
  currentGame: DartsGame,
  scope: CoroutineScope,
  viewModel: OhOneViewModel,
  navigateToHistory: () -> Unit,
  gameStatus: GameStatus
) {

  when (gameStatus) {
    is GameStatus.InProgress -> {
      // Do nothing
    }
    is GameStatus.Finished -> {
      DartsWinner(winner = gameStatus.winner, navigateToHistory = navigateToHistory)
    }
  }

  // This mutable state ensures that the player composables will update when a score is entered
  var players by remember { mutableStateOf((currentGame.gameScore as GameScore.OhOneScore).players) }
  var currentPlayerIndex by remember { mutableStateOf(0) }
  // This  will ensure we update the "Round" text in the CricketScores composable
  var currentRound by remember { mutableStateOf(1) }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarState) }
  ) {
    Column(
      modifier = Modifier.padding(it),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {

        // This needs to listen to the State object so that the player objects recompose when they change
        players.forEachIndexed { index, ohOnePlayer ->
          OnOnePlayerStats(player = ohOnePlayer, isCurrentPlayer = players[currentPlayerIndex] == ohOnePlayer)
          // We want the scores to be as close to the middle as possible. If we're in the middle of the list, add the
          // scores so the players straddle the scores
          if (index == viewModel.indexToAddScores(players.size)) {
            OhOneScoreEntry(
              isDoubleIn = (currentGame.gameScore as GameScore.OhOneScore).isDoubleIn,
              isDoubleOut = currentGame.gameScore.isDoubleOut,
              scoreLimit = currentGame.gameScore.scoreLimit,
              round = currentRound,
              updatePlayerScore = { scoreEntered ->
                // Create a mutable copy of the player list

                scope.launch {
                  players = viewModel.updateOhOnePlayer(
                    playerToUpdateIndex = currentPlayerIndex,
                    scoreEntered = scoreEntered,
                    playersList = players.toMutableList(),
                    game = currentGame,
                    round = currentRound
                  )

                  // If the current player is the last player in the list, roll over to the next round
                  if (currentPlayerIndex == (players.size - 1)) {
                    currentRound++
                    currentPlayerIndex = 0
                    scope.launch {
                      snackbarState.showSnackbar(
                        message = "Starting round $currentRound, it is ${players[currentPlayerIndex].name}'s turn"
                      )
                    }
                  } else {
                    currentPlayerIndex++
                    scope.launch {
                      snackbarState.showSnackbar(
                        message = "It is ${players[currentPlayerIndex].name}'s turn"
                      )
                    }
                  }
                }
              },
              viewModel = viewModel,
              currentPlayer = players[currentPlayerIndex],
            )
          }
        }
      }
    }
  }
}

@Composable
fun OhOneScoreEntry(
  isDoubleIn: Boolean,
  isDoubleOut: Boolean,
  scoreLimit: Int,
  round: Int,
  updatePlayerScore: (Int) -> Unit,
  currentPlayer: OhOnePlayer,
  viewModel: OhOneViewModel,
) {

  Column(
    modifier = Modifier
      .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    DartsText(
      value = "$scoreLimit -- Round $round",
      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    )
    DartsText(
      value = "Enter dart values:",
      style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
    )

    var dartOne by remember { mutableStateOf(OhOneDartValue()) }
    var dartTwo by remember { mutableStateOf(OhOneDartValue()) }
    var dartThree by remember { mutableStateOf(OhOneDartValue()) }

    var showingScoreAlert by remember { mutableStateOf(false) }
    var scoringScenario = OhOneScoreScenario.SCORED

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Dart One
      OhOneSingleDart(
        ohOneDartValue = dartOne,
        onDartValueChanged = {
          dartOne = dartOne.copy(dartValue = it)
        }, onDoubleChanged = {
          dartOne = dartOne.copy(isDoubled = it)
        }, onTripleChanged = {
          dartOne = dartOne.copy(isTripled = it)
        })
      // Dart Two
      OhOneSingleDart(
        ohOneDartValue = dartTwo,
        onDartValueChanged = {
          dartTwo = dartTwo.copy(dartValue = it)
        }, onDoubleChanged = {
          dartTwo = dartTwo.copy(isDoubled = it)
        }, onTripleChanged = {
          dartTwo = dartTwo.copy(isTripled = it)
        })
      // Dart Three
      OhOneSingleDart(
        ohOneDartValue = dartThree,
        onDartValueChanged = {
          dartThree = dartThree.copy(dartValue = it)
        }, onDoubleChanged = {
          dartThree = dartThree.copy(isDoubled = it)
        }, onTripleChanged = {
          dartThree = dartThree.copy(isTripled = it)
        })
    }
    Button(
      onClick = {
        val dartScores = listOf(dartOne, dartTwo, dartThree)
        when (val currentScoringScenario = viewModel.scoreShouldCount(
          playerCurrentScore = currentPlayer.scores.last(),
          scoreLimit = scoreLimit,
          isDoubleInGame = isDoubleIn,
          isDoubleOutGame = isDoubleOut,
          dartValues = dartScores
        )) {
          OhOneScoreScenario.SCORED -> {
            updatePlayerScore(viewModel.getRoundValue(dartScores))
            // In order to force recomposition of the Text Fields, we can reset the dart values to their defaults after
            // a successful score has been entered
            dartOne = OhOneDartValue()
            dartTwo = OhOneDartValue()
            dartThree = OhOneDartValue()
          }
          else -> {
            showingScoreAlert = true
            scoringScenario = currentScoringScenario
          }
        }
      }) {
      Text(text = "Enter score")
      if (showingScoreAlert) {
        OhOneScoringAlert(onDismiss = { showingScoreAlert = false }, scoringScenario = scoringScenario)
      }
    }
  }
}

@Composable
fun OhOneScoringAlert(
  onDismiss: (Boolean) -> Unit,
  scoringScenario: OhOneScoreScenario
) {
  // Show dialog saying first dart must be Double to score
  AlertDialog(
    onDismissRequest = { onDismiss(false) },
    title = { Text(text = "Scoring Error") },
    text = {
      Text(
        text =
        when (scoringScenario) {
          OhOneScoreScenario.NEEDS_DOUBLE_IN -> {
            "The first scoring dart must be a Double!"
          }
          OhOneScoreScenario.NEEDS_DOUBLE_OUT -> {
            "The last scoring dart must be a Double!"
          }
          else -> {
            "Somehow you broke the game..."
          }
        }
      )
    },
    confirmButton = {
      TextButton(onClick = { onDismiss(false) }) {
        Text(text = "Ok")
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhOneSingleDart(
  ohOneDartValue: OhOneDartValue,
  onDartValueChanged: (Int) -> Unit,
  onDoubleChanged: (Boolean) -> Unit,
  onTripleChanged: (Boolean) -> Unit
) {
  val focusManager = LocalFocusManager.current
  OutlinedTextField(
    value = ohOneDartValue.dartValue.toString(),
    onValueChange = { newValue ->
      onDartValueChanged(newValue.toIntOrNull() ?: 0)
    },
    label = { Text(text = "Dart Value") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
    singleLine = true,
    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
  )
  Row(verticalAlignment = Alignment.CenterVertically)  {
    Checkbox(
      checked = ohOneDartValue.isDoubled,
      onCheckedChange = { newValue ->
        onDoubleChanged(newValue)
      },
      // A dart cannot be both a triple and a double. If one is checked, disable the other
      enabled = !ohOneDartValue.isTripled
    )
    Text(text = "Double")
  }
  Row(verticalAlignment = Alignment.CenterVertically)  {
    Checkbox(
      checked = ohOneDartValue.isTripled,
      onCheckedChange = { newValue ->
        onTripleChanged(newValue)
      },
      // A dart cannot be both a triple and a double. If one is checked, disable the other
      enabled = !ohOneDartValue.isDoubled
    )
    Text(text = "Triple")
  }

}

@Composable
fun OnOnePlayerStats(
  player: OhOnePlayer,
  isCurrentPlayer: Boolean
) {
  Box(
    modifier = Modifier
      .background(color = if (isCurrentPlayer) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
      .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
  ) {
    LazyColumn(
      modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp)),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        DartsText(
          value = player.name,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
      itemsIndexed(items = player.scores) { index, score ->
        DartsText(
          value = score.toString(),
          // All but the last line should be struck through since they're old scores
          shouldStrikethrough = index != (player.scores.size - 1)
        )
      }
    }
  }
}

data class OhOneDartValue(
  val dartValue: Int = 0,
  val isDoubled: Boolean = false,
  val isTripled: Boolean = false
)

