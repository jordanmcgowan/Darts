package com.darts.ui.game

import androidx.lifecycle.ViewModel
import com.darts.data.DartsGame
import com.darts.data.DartsRepository
import com.darts.data.GameScore
import com.darts.data.OhOnePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OhOneViewModel @Inject constructor(
  val dartsRepository: DartsRepository
) : ViewModel() {
  // TODO
  private val mutableGameState = MutableStateFlow<GameStatus>(GameStatus.InProgress)
  val gameState: StateFlow<GameStatus> = mutableGameState.asStateFlow()

  suspend fun updateOhOnePlayer(
    playerToUpdateIndex: Int,
    scoreEntered: Int,
    game: DartsGame,
    playersList: MutableList<OhOnePlayer>,
    round: Int
  ): List<OhOnePlayer> {
    var playerToUpdate = playersList[playerToUpdateIndex]

    // Copy the current score list
    val scoreCopy = playerToUpdate.scores.toMutableList()
    // Get the new score
    val newScore = scoreCopy.last() - scoreEntered

    // If the new score is below 0, the player busted and this round shouldn't count. Also, if the game is Double Out
    // and the player gets to 1 point, they bust (as there is no way to get a 1 with a double).
    // In this case, we'll return the previous set of scores (which is the same as the values passed in)
    playersList[playerToUpdateIndex] = if (newScore < 0 || ((game.gameScore as GameScore.OhOneScore).isDoubleOut && newScore == 1)) {
      // On a bust, add the old score to the list again to show there was a bust
      scoreCopy.add(scoreCopy.last())
      playerToUpdate.copy(scores = scoreCopy)
    } else {
      // Add the new score to the list
      scoreCopy.add(newScore)
      // Return the updated score
      playerToUpdate.copy(scores = scoreCopy)
    }

    // Ensure the rest of the method has the most updated player info
    playerToUpdate = playersList[playerToUpdateIndex]

    val didPlayerWin = playerToUpdate.scores.last() == 0

    if (didPlayerWin) {
      mutableGameState.emit(GameStatus.Finished(playerToUpdate))
    }

    updateGame(
      game = game.copy(
        gameScore = (game.gameScore as GameScore.OhOneScore).copy(
          players = playersList
        ),
        round = round,
        winner = if (didPlayerWin) playerToUpdate else null
      )
    )

    return playersList

  }

  fun scoreShouldCount(
    playerCurrentScore: Int,
    scoreLimit: Int,
    isDoubleInGame: Boolean,
    isDoubleOutGame: Boolean,
    dartValues: List<OhOneDartValue>
  ): OhOneScoreScenario {
    return when {
      isDoubleInGame -> {
        if (playerCurrentScore == scoreLimit && (dartValues.firstOrNull { it.dartValue != 0 }?.isDoubled == false)){
          OhOneScoreScenario.NEEDS_DOUBLE_IN
        } else {
          OhOneScoreScenario.SCORED
        }
      }
      isDoubleOutGame -> {
        if (playerCurrentScore - getRoundValue(dartValues) == 0 && (dartValues.lastOrNull { it.dartValue != 0 }?.isDoubled == false)) {
          OhOneScoreScenario.NEEDS_DOUBLE_OUT
        } else {
          OhOneScoreScenario.SCORED
        }
      }
      else -> {
        OhOneScoreScenario.SCORED
      }
    }
  }

  fun indexToAddScores(numberOfPlayers: Int): Int {
    return when (numberOfPlayers) {
      1, 2 -> 0
      3, 4 -> 1
      else -> throw UnsupportedOperationException()
    }
  }

  suspend fun updateGame(game: DartsGame) {
    dartsRepository.updateGame(game)
  }

  fun getRoundValue(
    dartValues: List<OhOneDartValue>
  ): Int {
    return dartValues.sumOf {
      when {
        it.isDoubled -> it.dartValue * 2
        it.isTripled -> it.dartValue * 3
        else -> it.dartValue
      }
    }
  }
}

enum class OhOneScoreScenario {
  NEEDS_DOUBLE_IN,
  NEEDS_DOUBLE_OUT,
  SCORED,
}