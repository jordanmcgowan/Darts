package com.darts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DartsDao {
  @Query("SELECT * FROM games")
  fun getAllGames(): Flow<List<DartsGame>>

  @Query("SELECT DISTINCT gameScore FROM games")
  fun getAllScores(): Flow<List<GameScore>>

  @Query("SELECT * FROM games")
  suspend fun getAllGamesSuspend(): List<DartsGame>

  @Query("SELECT * FROM games WHERE timestamp is :gameTimestamp LIMIT 1")
  suspend fun getGameByTimestamp(gameTimestamp: Long): DartsGame

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addGame(game: DartsGame)

  @Update
  suspend fun updateGame(game: DartsGame)

  @Delete
  suspend fun deleteGame(game: DartsGame)
}