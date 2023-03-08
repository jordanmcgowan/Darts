package com.darts.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darts.data.DartsGame
import com.darts.data.DartsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
  private val dartsRepository: DartsRepository
) : ViewModel() {

  private val historyViewState = MutableStateFlow<HistoryViewState>(HistoryViewState.Loading)
  val viewState: StateFlow<HistoryViewState> = historyViewState.asStateFlow()

  suspend fun getAllGames() {
    val games = dartsRepository.getAllGames()
    historyViewState.emit(HistoryViewState.Content(games))
  }

  // TODO - Add delete functionality
  fun delete(dartsGame: DartsGame) = viewModelScope.launch {
    historyViewState.emit(HistoryViewState.Loading)
    dartsRepository.deleteGame(dartsGame).runCatching {
      getAllGames()
    }
  }

}

sealed class HistoryViewState{
  object Loading: HistoryViewState()
  data class Content(val games: List<DartsGame>): HistoryViewState()
  object Failure: HistoryViewState()

}