package com.darts.data

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Transform a  [GameScore] to/from a serialized json object.
 *
 * Room doesn't support storing string arrays natively. This gets around that by creating a json array from the list and
 * storing that as a String in the database.
 */
object GameScoreConvertor {
  @TypeConverter
  fun fromScoreToString(value: GameScore) = Json.encodeToString(value)

  @TypeConverter
  fun toScoreFromString(value: String) = Json.decodeFromString<GameScore>(value)
}

object DartsPlayerConvertor {
  @TypeConverter
  fun fromPlayerToString(value: DartsPlayer?) = if (value == null) null else Json.encodeToString(value)

  @TypeConverter
  fun toPlayerFromString(value: String?) = if (value == null) null else Json.decodeFromString<DartsPlayer>(value)
}