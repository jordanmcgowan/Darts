package com.darts.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.darts.MainViewModel
import com.darts.R
import com.darts.data.CricketMarks
import com.darts.data.CricketPlayer
import com.darts.data.DartsGame
import com.darts.data.GameScore
import com.darts.data.GameVariation
import com.darts.data.OhOnePlayer
import com.darts.ui.game.DartBoardMark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  startGame: (Long) -> Unit,
  viewModel: MainViewModel = hiltViewModel()
) {
  Scaffold {
    Column(
      modifier = Modifier
        .padding(paddingValues = it)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = CenterHorizontally
    ) {
      Text(
        text = "Welcome to J's Place Darts",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.align(CenterHorizontally)
      )
      Text(
        text = "Choose your game below",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.align(CenterHorizontally)
      )
      Row {
        GameCell(
          onClick = { gameTimestamp -> startGame(gameTimestamp) },
          gameVariation = GameVariation.CRICKET,
          viewModel = viewModel
        )
        GameCell(
          onClick = { gameTimestamp -> startGame(gameTimestamp) },
          gameVariation = GameVariation.OH_ONE,
          viewModel = viewModel
        )
      }
    }
  }
}

@Composable
fun GameCell(
  onClick: (Long) -> Unit,
  gameVariation: GameVariation,
  viewModel: MainViewModel
) {
  Column(
    modifier = Modifier
      .padding(12.dp)
      .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
      .padding(12.dp)
  ) {
    var playerCount by remember { mutableStateOf(1) }

    Text(
      text = gameVariation.displayName,
      style = MaterialTheme.typography.headlineMedium
    )

    Row {
      Text(text = "Number of players:")
      Icon(
        painter = painterResource(id = R.drawable.subtract),
        modifier = Modifier
          .clickable {
            if (playerCount > 1) {
              playerCount--
            } else { /* do nothing */
            }
          }
          .alpha(
            if (playerCount > 1) {
              1f
            } else {
              .2f
            }
          ),
        contentDescription = null
      )
      Text(text = playerCount.toString())
      Icon(
        imageVector = Icons.Filled.Add,
        modifier = Modifier
          .clickable {
            if (playerCount < 4) {
              playerCount++
            } else { /* do nothing */
            }
          }
          .alpha(
            if (playerCount < 4) {
              1f
            } else {
              .2f
            }
          ),
        contentDescription = null)
    }

    var playerOneName by remember { mutableStateOf("") }
    var playerTwoName by remember { mutableStateOf<String?>(null) }
    var playerThreeName by remember { mutableStateOf<String?>(null) }
    var playerFourName by remember { mutableStateOf<String?>(null) }
    PlayerNameField(
      playerHint = "Player 1 name",
      playerName = playerOneName,
      onChange = { newValue -> playerOneName = newValue },
    )
    if (playerCount > 1) {
      PlayerNameField(
        playerHint = "Player 2 name",
        playerName = playerTwoName ?: "",
        onChange = { newValue -> playerTwoName = newValue }
      )
    }
    if (playerCount > 2) {
      PlayerNameField(
        playerHint = "Player 3 name",
        playerName = playerThreeName ?: "",
        onChange = { newValue -> playerThreeName = newValue }
      )
    }
    if (playerCount > 3) {
      PlayerNameField(
        playerHint = "Player 4 name",
        playerName = playerFourName ?: "",
        onChange = { newValue -> playerFourName = newValue }
      )
    }

    var infoDialogState by remember { mutableStateOf<DialogInfoState?>(null) }

    when (gameVariation) {
      GameVariation.OH_ONE -> {
        var scoreLimit by remember { mutableStateOf(301) }
        OhOneScoreLimitEntry(
          scoreLimit = scoreLimit.toString(),
          onChange = { newValue ->
            val entry = newValue.toIntOrNull()
            if (entry != null) {
              scoreLimit = entry
            }
          },
          scoreHint = "Score limit"
        )

        var doubleIn by remember { mutableStateOf(false) }
        var doubleOut by remember { mutableStateOf(false) }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Double In?")
          Checkbox(checked = doubleIn, onCheckedChange = { newValue -> doubleIn = newValue })
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.DoubleIn })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Double Out?")
          Checkbox(checked = doubleOut, onCheckedChange = { newValue -> doubleOut = newValue })
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.DoubleOut })
        }

        // These items make a game valid
        val playersHaveUniqueNames = setOfNotNull(playerOneName, playerTwoName, playerThreeName, playerFourName).size == playerCount
        // TODO - add more criteria?
        val gameIsValid = playersHaveUniqueNames

        // TODO -- this isn't creating a row...
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = {
              val game = DartsGame(
                gameVariation = gameVariation,
                gameScore = GameScore.OhOneScore(
                  isDoubleIn = doubleIn,
                  isDoubleOut = doubleOut,
                  players = listOfNotNull(
                    OhOnePlayer(playerName = playerOneName.trim(), scores = listOf(scoreLimit)),
                    playerTwoName?.let { validPlayerTwoName ->
                      OhOnePlayer(
                        playerName = validPlayerTwoName.trim(),
                        scores = listOf(scoreLimit)
                      )
                    },
                    playerThreeName?.let { validPlayerThreeName ->
                      OhOnePlayer(
                        playerName = validPlayerThreeName.trim(),
                        scores = listOf(scoreLimit)
                      )
                    },
                    playerFourName?.let { validPlayerFourName ->
                      OhOnePlayer(
                        playerName = validPlayerFourName.trim(),
                        scores = listOf(scoreLimit)
                      )
                    }
                  ),
                  scoreLimit = scoreLimit
                ),
              )
              viewModel.insert(dartsGame = game)
              onClick(game.timestamp)
            },
            enabled = gameIsValid
          ) {
            Text(text = "Start game")
          }
        }
        if (!gameIsValid) {
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.InvalidGame })
        }
      }
      GameVariation.CRICKET -> {
        var isCutThroat by remember { mutableStateOf(false) }
        var isQuickrit by remember { mutableStateOf(false) }
        var isRandomNumbers by remember { mutableStateOf(false) }

        Text(
          text = "Game Variations:",
          style = MaterialTheme.typography.headlineSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = isCutThroat, onCheckedChange = { newValue -> isCutThroat = newValue })
          Text(text = "Cutthroat")
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.Cutthroat })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = isQuickrit, onCheckedChange = { newValue -> isQuickrit = newValue })
          Text(text = "Quick-rit")
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.Quickrit })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = isRandomNumbers, onCheckedChange = { newValue -> isRandomNumbers = newValue })
          Text(text = "Random Numbers")
          DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.RandomNum })
        }

        val marksToHit = if (isRandomNumbers) {
          // Take 6 Random numbers, sort them top down, then add the Bull
          DartBoardMark.fullMarks.shuffled().take(6).sortedByDescending { it.pointValue }.plus(DartBoardMark.BULLSEYE)
        } else {
          DartBoardMark.cricketMarks
        }

        val playerMarks = marksToHit.map {
          CricketMarks(
            mark = it,
            // Quickrit only has the players needing to get 2 marks to close a round, hence "Quick".
            // Bullseyes are always points in Quickrit (which is handled in the CricketVM), so they'll be "closed" already
            numberOfHits = when {
              it == DartBoardMark.BULLSEYE && isQuickrit -> 3
              isQuickrit -> 1
              else -> 0
            }
          )
        }

        // These items make a game valid
        val playersHaveUniqueNames =
          setOfNotNull(playerOneName, playerTwoName, playerThreeName, playerFourName).size == playerCount
        // TODO - add more criteria?
        val gameIsValid = playersHaveUniqueNames

        // TODO -- this isn't creating a row...
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(onClick = {
            val game = DartsGame(
              gameVariation = gameVariation,
              gameScore = GameScore.CricketScore(
                isCutThroat = isCutThroat,
                isQuickrit = isQuickrit,
                players = listOfNotNull(
                  CricketPlayer(playerName = playerOneName.trim(), marks = playerMarks),
                  playerTwoName?.let { validPlayerTwoName ->
                    CricketPlayer(
                      playerName = validPlayerTwoName.trim(),
                      marks = playerMarks
                    )
                  },
                  playerThreeName?.let { validPlayerThreeName ->
                    CricketPlayer(
                      playerName = validPlayerThreeName.trim(),
                      marks = playerMarks
                    )
                  },
                  playerFourName?.let { validPlayerFourName ->
                    CricketPlayer(
                      playerName = validPlayerFourName.trim(),
                      marks = playerMarks
                    )
                  }
                )
              ),
            )
            viewModel.insert(dartsGame = game)
            onClick(game.timestamp)
          }) {
            Text(text = "Start game")
          }
          if (!gameIsValid) {
            DartRuleIconButton(setDialogInfoState = { infoDialogState = DialogInfoState.InvalidGame })
          }
        }
      }
    }
    if (infoDialogState != null) {
      GameRulesDialog(
        onDismiss = { infoDialogState = null },
        infoDialogState = infoDialogState!!
      )
    }
  }
}

@Composable
fun DartRuleIconButton(
  setDialogInfoState: () -> Unit
) {
  IconButton(onClick = setDialogInfoState) {
    Icon(imageVector = Icons.Default.Info, modifier = Modifier.padding(8.dp), contentDescription = null)
  }
}


@Composable
fun GameRulesDialog(
  onDismiss: () -> Unit,
  infoDialogState: DialogInfoState
) {
  AlertDialog(
    title = { Text(text = "Game Rules") },
    text = { Text(text = infoDialogState.content) },
    onDismissRequest = { onDismiss() },
    confirmButton = {
      TextButton(onClick = { onDismiss() }) {
        Text(text = "Ok")
      }
    }
  )
}

@Composable
fun OhOneScoreLimitEntry(
  scoreHint: String,
  scoreLimit: String,
  onChange: (String) -> Unit
) {
  CustomTextField(
    hintText = scoreHint,
    value = scoreLimit,
    onChange = onChange,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done),
    dropdownOptions = listOf("101", "301", "501", "701", "901")
  )
  
}

@Composable
fun PlayerNameField(
  playerHint: String,
  playerName: String,
  onChange: (String) -> Unit,
  viewModel: MainViewModel = hiltViewModel()
) {

  val players = mutableSetOf<String>()
  viewModel.allScores.observeAsState().value?.map {
    when (it) {
      is GameScore.OhOneScore -> {
        players.addAll(it.players.map { player -> player.name })
      }
      is GameScore.CricketScore -> {
        players.addAll(it.players.map { player -> player.name })
      }
    }
  }

  CustomTextField(
    hintText = playerHint,
    value = playerName,
    onChange = onChange,
    keyboardOptions = KeyboardOptions(
      // Let players spell their names how they'd like!
      autoCorrect = false,
      capitalization = KeyboardCapitalization.Words,
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done),
    dropdownOptions = players.toList()
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
  hintText: String,
  value: String,
  onChange: (String) -> Unit,
  keyboardOptions: KeyboardOptions,
  dropdownOptions: List<String>
) {
  val focusManager = LocalFocusManager.current

  OutlinedTextField(
    modifier = Modifier.padding(vertical = 8.dp),
    value = value,
    onValueChange = { onChange(it) },
    label = { Text(text = hintText) },
    isError = value == "",
    supportingText = {
      if (value == "") {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = "Field cannot be empty",
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    trailingIcon = {
      var nameSelectorDropdownExpanded by remember { mutableStateOf(false) }

      // options button
      IconButton(onClick = {
        nameSelectorDropdownExpanded = true
      }) {
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "Open Options"
        )
      }

      DropdownMenu(
        expanded = nameSelectorDropdownExpanded,
        onDismissRequest = {
          nameSelectorDropdownExpanded = false
        }
      ) {
        dropdownOptions.forEach {
          DropdownMenuItem(
            onClick = {
              onChange(it)
              nameSelectorDropdownExpanded = false
            },
            enabled = true,
            text = { Text(text = it)}
          )
        }

      }
    },
    keyboardOptions = keyboardOptions,
    singleLine = true,
    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
  )

}

@Preview
@Composable
fun HomeScreenDefaultPreview() {
  PlayerNameField(
    playerName = "",
    playerHint = "Player 1",
    onChange = { /* TODO */ }
  )
}

enum class DialogInfoState(val content: String) {
  Cutthroat(content = "In Cutthroat, you give points to other players when you score"),
  Quickrit(content = "Quickrit is a speedy version of Cricket where all numbers start with one mark and BULLS are always worth points"),
  RandomNum(content = "Random Numbers gives you an opportunity to throw darts at new numbers! Same rules apply as all other games, but you'll have 6 random numbers and the BULL to shoot at"),
  InvalidGame(content = "This game is invalid. Check to ensure all players have unique names"),
  DoubleIn(content = "Your first scoring dart MUST be a double"),
  DoubleOut(content = "Your last scoring dart MUST be a double"),
}
