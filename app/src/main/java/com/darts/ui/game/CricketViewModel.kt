package com.darts.ui.game

import androidx.lifecycle.ViewModel
import com.darts.data.CricketPlayer
import com.darts.data.DartsGame
import com.darts.data.DartsRepository
import com.darts.data.GameScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CricketViewModel @Inject constructor(
  private val dartsRepository: DartsRepository
) : ViewModel() {

  private val mutableGameState = MutableStateFlow<GameStatus>(GameStatus.InProgress)
  val gameState: StateFlow<GameStatus> = mutableGameState.asStateFlow()

  suspend fun updateCricketPlayer(
    playerToUpdateIndex: Int,
    playerListCopy: MutableList<CricketPlayer>,
    markRecorded: DartBoardMark,
    game: DartsGame,
    round: Int
  ): List<CricketPlayer> {
    var listDupe = playerListCopy.toMutableList()
    val playerToUpdate = listDupe[playerToUpdateIndex]

    val markCopy = playerToUpdate.marks.toMutableList()
    val markToUpdate = playerToUpdate.marks.find { it.mark == markRecorded }!!

    // If the player doesn't yet have three marks on this digit, increment the marks
    listDupe[playerToUpdateIndex] = if (markToUpdate.numberOfHits < 3) {
      markCopy[playerToUpdate.marks.indexOf(markToUpdate)] =
        markToUpdate.copy(numberOfHits = markToUpdate.numberOfHits + 1)
      playerToUpdate.copy(marks = markCopy)
    } else {
      // If the mark is still open (i.e. not ALL players have three marks), dole out points based on game type
      if (isMarkOpen(
          players = listDupe,
          markInQuestion = markRecorded
        ) || ((game.gameScore as GameScore.CricketScore).isQuickrit && markRecorded == DartBoardMark.BULLSEYE)
      ) {
        if ((game.gameScore as GameScore.CricketScore).isCutThroat) {
          if (game.gameScore.isQuickrit && markRecorded == DartBoardMark.BULLSEYE) {
            // In quickrit, the bullseye is always points, so dole out the points regardless of existing marks
            listDupe = listDupe.map {
              it.copy(pointTotal = it.pointTotal + markRecorded.pointValue)
            }.toMutableList()
            // Since nothing happens to the active player, just return the original stats to keep the list in tact
            playerToUpdate
          } else {
            // In cutthroat, all other players are given points for the mark hit
            listDupe = listDupe.findAndTransform(
              { player -> (player.marks.find { it.mark == markRecorded }?.numberOfHits ?: 0) < 3 },
              { it.copy(pointTotal = it.pointTotal + markRecorded.pointValue) }
            ) as MutableList<CricketPlayer>
            // Since nothing happens to the active player, just return the original stats to keep the list in tact
            playerToUpdate
          }
        } else {
          // In standard, the player who threw the dart gets the points
          playerToUpdate.copy(pointTotal = playerToUpdate.pointTotal + markRecorded.pointValue)
        }
      } else {
        // Otherwise, the group is closed and the player gets no points
        playerToUpdate
      }
    }

    val playerWon = didPlayerWin(
      playerToUpdateIndex = playerToUpdateIndex,
      otherPlayers = listDupe,
      game = game
    )



    if (playerWon) {
      // Emit that a player won
      mutableGameState.emit(GameStatus.Finished(playerToUpdate))
    }

    // Update the game to contain the current stats (and maybe the winner if that's the state of the game
    updateGame(
      game = game.copy(
        gameScore = (game.gameScore as GameScore.CricketScore).copy(
          players = listDupe
        ),
        round = round,
        winner = if (playerWon) playerToUpdate else null
      )
    )


    return listDupe
  }

  private fun didPlayerWin(
    playerToUpdateIndex: Int,
    otherPlayers: List<CricketPlayer>,
    game: DartsGame
  ): Boolean {
    val currentPlayer = otherPlayers[playerToUpdateIndex]
    val listWithoutCurrentPlayer = otherPlayers.toMutableList()
    // Create a copy of the list without the current player as we want to ensure their score is higher (or lower) than
    // all the rest
    listWithoutCurrentPlayer.removeAt(playerToUpdateIndex)
    // Ensure they have all marks closed
    val allMarksClosed = currentPlayer.marks.all { it.numberOfHits == 3 }

    val meetsScoringRequirement = when {
      listWithoutCurrentPlayer.isEmpty() -> {
        // In single player games, this list will be empty and `maxOf` will through an exception -- we'll cover our bases
        // by knowing the player meets this Req as there is no one else to score more points than them
        true
      }
      ((game.gameScore as GameScore.CricketScore).isCutThroat) -> {
        currentPlayer.pointTotal < listWithoutCurrentPlayer.maxOf { it.pointTotal }
      }
      else -> {
        currentPlayer.pointTotal > listWithoutCurrentPlayer.maxOf { it.pointTotal }
      }
    }

    return allMarksClosed && meetsScoringRequirement
  }

  fun indexToAddScores(numberOfPlayers: Int): Int {
    return when (numberOfPlayers) {
      1, 2 -> 0
      3, 4 -> 1
      else -> throw UnsupportedOperationException()
    }
  }

  private suspend fun updateGame(game: DartsGame) {
    dartsRepository.updateGame(game)
  }

  fun isMarkOpen(
    players: List<CricketPlayer>,
    markInQuestion: DartBoardMark
  ): Boolean {
    return players.any { player -> (player.marks.find { it.mark == markInQuestion }?.numberOfHits ?: 0) < 3 }
  }
}

fun <T> MutableList<T>.findAndTransform(predicate: (T) -> Boolean, transform: (T) -> T): List<T> {
  return map { if (predicate(it)) transform(it) else it }
}