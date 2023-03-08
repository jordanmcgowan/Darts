package com.darts.ui.history

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.darts.data.CricketPlayer
import com.darts.data.DartsGame
import com.darts.data.DartsPlayer
import com.darts.data.GameScore
import com.darts.data.GameVariation
import com.darts.data.OhOnePlayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  viewModel: HistoryViewModel = hiltViewModel(),
  navigateToGame: (Long) -> Unit
) {
  LaunchedEffect(viewModel) {
    viewModel.getAllGames()
  }

  val viewState: HistoryViewState by viewModel.viewState.collectAsState()

  when (viewState) {
    is HistoryViewState.Content -> {
      HistoryList(
        allGames = (viewState as HistoryViewState.Content).games,
        navigateToGame = navigateToGame,
        deleteGame = { dartsGame -> viewModel.delete(dartsGame) }
      )
    }
    HistoryViewState.Failure -> {
      // TODO - Error Dialog?
    }
    HistoryViewState.Loading -> LoadingSpinner()
  }
}

@Composable
fun LoadingSpinner() {
  Box(
    modifier = Modifier.size(200.dp),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
fun HistoryList(
  allGames: List<DartsGame>?,
  navigateToGame: (Long) -> Unit,
  deleteGame: (DartsGame) -> Unit
) {
  if (allGames.isNullOrEmpty()) {
    EmptyGameList()
  } else {
    GameList(
      allGames = allGames,
      navigateToGame = navigateToGame,
      deleteGame = deleteGame
    )
  }
}

@Composable
fun EmptyGameList() {
  Box(modifier = Modifier.padding(16.dp)){
    Text(
      text = "No games found",
      style = MaterialTheme.typography.headlineLarge
    )
  }
}

@Composable
fun GameList(
  allGames: List<DartsGame>,
  navigateToGame: (Long) -> Unit,
  deleteGame: (DartsGame) -> Unit
) {
  var gameToDelete by remember { mutableStateOf<DartsGame?>(null) }
  LazyColumn(
    modifier = Modifier.fillMaxHeight(),
    contentPadding = PaddingValues(all = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = "Game History",
        style = MaterialTheme.typography.headlineLarge
      )
    }
    // Since the games are stored oldest to newest, we'll flip the list to show the most recent games at the top
    items(items = allGames.reversed()) { game ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
          .clickable(enabled = true, onClick = { navigateToGame(game.timestamp) })
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {

          Column {
            GameTitle(game = game)
            val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val dateString = formatter.format(Date(game.timestamp))
            Text(text = "Game date: $dateString")
            if (game.winner != null) {
              FinishedGame(winner = game.winner, game = game)
            } else {
              InProgressGame(game = game)
            }
          }
          Spacer(Modifier.weight(1f))
          IconButton(
            modifier = Modifier
              .size(80.dp),
            onClick = { gameToDelete = game}
          ) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = null
            )
          }

          if (gameToDelete != null) {
            DeleteGameConfirmation(
              onDismiss = { gameToDelete = null },
              onDelete = { deleteGame(gameToDelete!!) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun DeleteGameConfirmation(
  onDismiss: () -> Unit,
  onDelete: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Delete game?") },
    text = { Text("This can cannot be restored once it's deleted. Are you sure you want to delete it?") },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "No")
      }
    },
    confirmButton = {
      TextButton(onClick = onDelete) {
        Text(text = "Yes")
      }
    }
  )

}

@Composable
fun GameTitle(game: DartsGame) {

  val gameTitle = buildAnnotatedString {
    when (game.gameVariation) {
      GameVariation.CRICKET -> {
        if ((game.gameScore as GameScore.CricketScore).isCutThroat) {
          append("Cuthroat ")
        }
        if (game.gameScore.isQuickrit) {
          append("Quickrit ")
        } else {
          append("Cricket ")
        }
      }
      GameVariation.OH_ONE -> {
        if ((game.gameScore as GameScore.OhOneScore).isDoubleIn) {
          append("Double In ")
        }
        if (game.gameScore.isDoubleOut) {
          append("Double Out ")
        }
        append(game.gameScore.scoreLimit.toString())
      }
    }
  }

  Text(text = gameTitle, fontSize = 24.sp)
}

@Composable
fun InProgressGame(
  game: DartsGame
) {
  val gamePlayers = when (game.gameVariation) {
    GameVariation.CRICKET -> (game.gameScore as GameScore.CricketScore).players.map { it.name }
    GameVariation.OH_ONE -> (game.gameScore as GameScore.OhOneScore).players.map { it.name }
  }
  Text(text = "This game between ${gamePlayers.joinToString(separator = ", ")} is still in progress.")
}

@Composable
fun FinishedGame(
  winner: DartsPlayer,
  game: DartsGame
) {
  val gamePlayers = when (game.gameVariation) {
    GameVariation.CRICKET -> (game.gameScore as GameScore.CricketScore).players
    GameVariation.OH_ONE -> (game.gameScore as GameScore.OhOneScore).players
  }
  val playerNameList = gamePlayers.filter { it.name != winner.name }

  Text(text = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
      append(winner.name)
    }
    // Add whitespace after name
    append(" ")
    if (game.gameVariation == GameVariation.CRICKET) {
      append("(")
      append((game.winner as CricketPlayer).pointTotal.toString())
      append(") ")
    }

    if (playerNameList.isEmpty()) {
      append("won ")
    } else {
      // Only include the names of the other players if there were any
      playerNameList.forEach {
        append("beat ")
        append(it.name)
        val pointsToShow = when (game.gameVariation) {
          GameVariation.CRICKET -> (it as CricketPlayer).pointTotal.toString()
          GameVariation.OH_ONE -> (it as OhOnePlayer).scores.last().toString()
        }
        append(" (${pointsToShow}) ")
      }
    }
    append("in ${game.round} rounds")
  }
  )
}

@Preview
@Composable
fun AlertPreview() {
  DeleteGameConfirmation(
    onDismiss = {},
    onDelete = {}
  )
}

@Preview
@Composable
fun LoadingSpinnerPreview() {
  LoadingSpinner()
}

@Preview
@Composable
fun MultiplayerStandardCricketWithWinner() {
  GameList(
    allGames = listOf(
      DartsGame(
        gameVariation = GameVariation.CRICKET,
        gameScore = GameScore.CricketScore(
          players = listOf(
            CricketPlayer(
              playerName = "Jordan",
              pointTotal = 34
            ),
            CricketPlayer(
              playerName = "Ben A",
              pointTotal = 34
            )
          ),
          isCutThroat = false,
          isQuickrit = false
        ),
        winner = CricketPlayer(
          playerName = "Jordan",
          pointTotal = 34
        )
      )
    ),
    navigateToGame = {},
    deleteGame = {}
  )
}

@Preview
@Composable
fun SinglePlayerStandardCricketWithWinner() {
  GameList(
    allGames = listOf(
      DartsGame(
        gameVariation = GameVariation.CRICKET,
        gameScore = GameScore.CricketScore(
          players = listOf(
            CricketPlayer(
              playerName = "Jordan",
              pointTotal = 34
            )
          ),
          isCutThroat = false,
          isQuickrit = false
        ),
        winner = CricketPlayer(
          playerName = "Jordan",
          pointTotal = 34
        )
      )
    ),
    navigateToGame = {},
    deleteGame = {}
  )
}

@Preview
@Composable
fun MultiplayerStandardCricketInProgress() {
  GameList(
    allGames = listOf(
      DartsGame(
        gameVariation = GameVariation.CRICKET,
        gameScore = GameScore.CricketScore(
          players = listOf(
            CricketPlayer(
              playerName = "Jordan",
              pointTotal = 34
            ),
            CricketPlayer(
              playerName = "Ben A",
              pointTotal = 12
            )
          ),
          isCutThroat = false,
          isQuickrit = false
        ),
      )
    ),
    navigateToGame = {},
    deleteGame = {}
  )
}

@Preview
@Composable
fun SinglePlayerStandardCricketInProgress() {
  GameList(
    allGames = listOf(
      DartsGame(
        gameVariation = GameVariation.CRICKET,
        gameScore = GameScore.CricketScore(
          players = listOf(
            CricketPlayer(
              playerName = "Jordan",
              pointTotal = 34
            )
          ),
          isCutThroat = false,
          isQuickrit = false
        )
      )
    ),
    navigateToGame = {},
    deleteGame = {}
  )
}