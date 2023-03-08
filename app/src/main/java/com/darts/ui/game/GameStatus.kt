package com.darts.ui.game

import com.darts.data.DartsPlayer

sealed class GameStatus {
  object InProgress: GameStatus()
  data class Finished(
    val winner: DartsPlayer
  ): GameStatus()
}
