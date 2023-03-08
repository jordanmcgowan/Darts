package com.darts.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class DartsRepository(private val dartsDao: DartsDao) {

  val allGames: Flow<List<DartsGame>> = dartsDao.getAllGames()

  val allScores: Flow<List<GameScore>> = dartsDao.getAllScores()

  @WorkerThread
  suspend fun getAllGames(): List<DartsGame> {
    return dartsDao.getAllGamesSuspend()
  }

  @WorkerThread
  suspend fun getGameByTimestamp(gameTimestamp: Long): DartsGame {
    return dartsDao.getGameByTimestamp(gameTimestamp = gameTimestamp)
  }

  @WorkerThread
  suspend fun addGame(game: DartsGame) {
    dartsDao.addGame(game)
  }

  @WorkerThread
  suspend fun updateGame(game: DartsGame) {
    dartsDao.updateGame(game)
  }

  @WorkerThread
  suspend fun deleteGame(game: DartsGame) {
    dartsDao.deleteGame(game)
  }
}