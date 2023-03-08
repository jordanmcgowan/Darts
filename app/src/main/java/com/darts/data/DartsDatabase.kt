package com.darts.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [(DartsGame::class)], version = 1, exportSchema = false)
@TypeConverters(GameScoreConvertor::class, DartsPlayerConvertor::class)
abstract class DartsDatabase : RoomDatabase() {

  abstract fun dartsDao(): DartsDao
}