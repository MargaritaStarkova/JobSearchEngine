package ru.practicum.android.diploma.favorite.domain

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.Logger
import ru.practicum.android.diploma.favorite.ui.FavoritesScreenState
import ru.practicum.android.diploma.root.BaseViewModel
import ru.practicum.android.diploma.util.thisName
import javax.inject.Inject

class FavoriteViewModel @Inject constructor(
    logger: Logger,
    private val favoritesInteractor: FavoritesInteractor,
) : BaseViewModel(logger) {


    private val uiStateMutable = MutableStateFlow<FavoritesScreenState>(FavoritesScreenState.Empty)
    val uiState: StateFlow<FavoritesScreenState> = uiStateMutable.asStateFlow()

    init { getListFavorites() }

    private fun getListFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            favoritesInteractor.getFavorites().collect{ list ->
                log(thisName, "favoritesInteractor.getFavorites().collect{ list -> $list")
                if (list.isEmpty()) {
                    uiStateMutable.emit(FavoritesScreenState.Empty)
                } else {
                    uiStateMutable.emit(FavoritesScreenState.Content(list))
                }
            }

        }
    }
    fun removeVacancy(id: Int) {
        log(thisName,"removeVacancy(id: $id)")
        viewModelScope.launch(Dispatchers.IO) {
            favoritesInteractor.removeVacancy(id)
        }
    }
}



