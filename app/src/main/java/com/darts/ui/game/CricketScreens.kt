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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.darts.R
import com.darts.data.CricketPlayer
import com.darts.data.DartsGame
import com.darts.data.GameScore
import com.darts.data.GameVariation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CricketGame(
  currentGame: DartsGame,
  navigateToHistory: () -> Unit,
  viewModel: CricketViewModel = hiltViewModel()
) {

  val gameState: GameStatus by viewModel.gameState.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  CricketGame(
    snackbarState = snackbarHostState,
    currentGame = currentGame,
    scope = scope,
    navigateToHistory = navigateToHistory,
    viewModel = viewModel,
    gameStatus = gameState
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricketGame(
  snackbarState: SnackbarHostState,
  currentGame: DartsGame,
  scope: CoroutineScope,
  navigateToHistory: () -> Unit,
  viewModel: CricketViewModel,
  gameStatus: GameStatus
) {

  // This mutable state ensures that the player composables will update when a score is entered
  var players by remember { mutableStateOf((currentGame.gameScore as GameScore.CricketScore).players) }
  var currentPlayerIndex by remember { mutableStateOf(0) }
  // This  will ensure we update the "Round" text in the CricketScores composable
  var currentRound by remember { mutableStateOf(currentGame.round) }

  when (gameStatus) {
    is GameStatus.InProgress -> {
      // Do nothing
    }
    is GameStatus.Finished -> {
      DartsWinner(winner = gameStatus.winner, navigateToHistory = navigateToHistory)
    }
  }

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
        players.forEachIndexed { index, cricketPlayer ->
          CricketPlayerStats(player = cricketPlayer, isCurrentPlayer = players[currentPlayerIndex] == cricketPlayer)
          // We want the scores to be as close to the middle as possible. If we're in the middle of the list, add the
          // scores so the players straddle the scores
          if (index == viewModel.indexToAddScores(players.size)) {
            CricketScores(
              isCutthroat = (currentGame.gameScore as GameScore.CricketScore).isCutThroat,
              isQuickrit = currentGame.gameScore.isQuickrit,
              round = currentRound,
              updatePlayerScore = { scoreEntered ->
                scope.launch {
                  players = viewModel.updateCricketPlayer(
                    playerToUpdateIndex = currentPlayerIndex,
                    playerListCopy = players.toMutableList(),
                    markRecorded = scoreEntered,
                    game = currentGame,
                    round = currentRound
                  )
                }
              },
              players = players,
              viewModel = viewModel
            )
          }
        }
      }
      Button(
        onClick = {
          // If the current player is the last player in the list, roll over to the next round
          val shouldEndRound = (currentPlayerIndex + 1) % players.size == 0
          // Increment Round
          if (shouldEndRound) {
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
        },
        modifier = Modifier.padding(16.dp).fillMaxWidth()
      ) {
        Text(text = "End Turn")
      }
    }
  }
}

@Composable
fun CricketPlayerStats(
  player: CricketPlayer,
  isCurrentPlayer: Boolean
) {
  Box(
    modifier = Modifier
      .background(color = if (isCurrentPlayer) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
      .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
  ) {
    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        DartsText(
          value = player.name,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
      item {
        DartsText(
          value = "Score: ${player.pointTotal}",
          style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
        )
      }
      items(items = player.marks) { marks ->
        when (marks.numberOfHits) {
          0 -> {
            // To keep the same spacing when the Icon is visible vs not, we'll put in a placeholder
            Icon(
              painter = painterResource(id = R.drawable.no_marks),
              contentDescription = null,
            )
          }
          1 -> {
            Icon(
              painter = painterResource(id = R.drawable.one_mark),
              contentDescription = null,
            )
          }
          2 -> {
            Icon(
              painter = painterResource(id = R.drawable.two_marks),
              contentDescription = null
            )
          }
          else -> {
            Icon(
              painter = painterResource(id = R.drawable.three_marks),
              contentDescription = null
            )
          }
        }
      }
    }
  }
}

@Composable
fun CricketScores(
  isCutthroat: Boolean,
  isQuickrit: Boolean,
  round: Int,
  updatePlayerScore: (DartBoardMark) -> Unit,
  players: List<CricketPlayer>,
  viewModel: CricketViewModel
) {
  LazyColumn(
    modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp)),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      DartsText(
        value = "${if (isCutthroat) "Cutthroat " else ""}Cricket -- Round $round",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
      )
    }
    item {
      DartsText(
        value = "Numbers in play",
        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
      )
    }
    items(items = players.first().marks) { item ->
      val isMarkOpen = when {
        // Bullseyes are ALWAYS points in Quickrit, so they should always be open
        isQuickrit && item.mark == DartBoardMark.BULLSEYE -> true
        else -> viewModel.isMarkOpen(players = players, markInQuestion = item.mark)
      }

      Button(
        onClick = { updatePlayerScore(item.mark) },
        enabled = isMarkOpen,
        colors = ButtonDefaults.buttonColors(
          containerColor = colorResource(id = R.color.green),
          disabledContainerColor = colorResource(id = R.color.red)
        )
      ) {
        Text(text = item.mark.displayName)
      }
    }
  }
}

@Preview
@Composable
fun OnePlayerPreview() {
  CricketGame(
    currentGame = DartsGame(
      timestamp = 12L,
      gameVariation = GameVariation.CRICKET,
      gameScore = GameScore.CricketScore(
        isCutThroat = false,
        players = listOf(
          CricketPlayer(
            playerName = "Jordan M",
          ),
          CricketPlayer(
            playerName = "Dan P",
          ),
          CricketPlayer(
            playerName = "Ben A",
          ),
          CricketPlayer(
            playerName = "Dylan C",
          )
        )
      )
    ),
    navigateToHistory = { }
  )
}