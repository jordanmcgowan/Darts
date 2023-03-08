package com.darts.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darts.ui.game.DartBoardMark
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Entity(tableName = "games")
data class DartsGame(
  @PrimaryKey(autoGenerate = false)
  val timestamp: Long = System.currentTimeMillis(),
  val gameVariation: GameVariation,
  val gameScore: GameScore? = null,
  val round: Int = 1,
  // A null winner should indicate a game is still in progress...
  val winner: DartsPlayer? = null
) : Parcelable

enum class GameVariation(val displayName: String) {
  CRICKET("Cricket"),
  OH_ONE("Oh One")
}

// This is necessary for the GameScoreConvertor to be able to be serialized using JSON
@Serializable
@Parcelize
sealed class GameScore : Parcelable {

  @Serializable
  @Parcelize
  data class CricketScore(
    val players: List<CricketPlayer> = emptyList(),
    val isCutThroat: Boolean = false,
    val isQuickrit: Boolean = false
  ): GameScore(), Parcelable

  @Serializable
  @Parcelize
  data class OhOneScore(
    val players: List<OhOnePlayer> = emptyList(),
    val isDoubleIn: Boolean = false,
    val isDoubleOut: Boolean = false,
    val scoreLimit: Int = 301
  ): GameScore(), Parcelable
}

@Serializable
@Parcelize
sealed class DartsPlayer(val name: String) : Parcelable

@Serializable
@Parcelize
data class CricketPlayer(
  val playerName: String = "",
  val marks: List<CricketMarks> = emptyList(),
  val pointTotal: Int = 0 , // To track points scored by player in traditional, points given by others in CUTTHROAT
): DartsPlayer(name = playerName), Parcelable

@Serializable
@Parcelize
data class OhOnePlayer(
  val playerName: String = "",
  val scores: List<Int> = emptyList() // Entry 0 will be the score limit, and new scores will be added
): DartsPlayer(name = playerName), Parcelable

@Serializable
@Parcelize
data class CricketMarks(
  val mark: DartBoardMark,
  val numberOfHits: Int = 0
): Parcelable
