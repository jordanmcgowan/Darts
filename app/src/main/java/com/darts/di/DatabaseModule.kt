package com.darts.di

import com.darts.data.DartsDao
import com.darts.data.DartsDatabase
import com.darts.data.DartsRepository
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule  {
  @Provides
  fun provideDao(dartsDatabase: DartsDatabase) : DartsDao {
    return dartsDatabase.dartsDao()
  }

  @Provides
  @Singleton
  fun provideDartsDatabase(@ApplicationContext appContext: Context):
    DartsDatabase {
    return Room.databaseBuilder(
      appContext,
      DartsDatabase::class.java,
      "darts_db"
    ).build()
  }

  @Provides
  fun providesRepository(dao: DartsDao) : DartsRepository = DartsRepository(dartsDao = dao)
}