package com.darts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darts.data.DartsGame
import com.darts.data.DartsRepository
import com.darts.data.GameScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val dartsRepository: DartsRepository
) : ViewModel() {

  private val gameViewState = MutableStateFlow<MainViewState>(MainViewState.Loading)
  val viewState: StateFlow<MainViewState> = gameViewState.asStateFlow()

  suspend fun getGame(gameTimestamp: Long) {
    val game = dartsRepository.getGameByTimestamp(gameTimestamp = gameTimestamp)
    gameViewState.emit(MainViewState.Content(game))
  }

  suspend fun getMostRecentGame() {
    val game = dartsRepository.getAllGames().last()
    gameViewState.emit(MainViewState.Content(game))
  }

  val allScores : LiveData<List<GameScore>> = dartsRepository.allScores.asLiveData()

  // Launching a new coroutine to insert the data in a non-blocking way
  fun insert(dartsGame: DartsGame) = viewModelScope.launch {
    dartsRepository.addGame(dartsGame)
  }

}

sealed class MainViewState{
  object Loading: MainViewState()
  data class Content(val game: DartsGame): MainViewState()
  object Failure: MainViewState()

}