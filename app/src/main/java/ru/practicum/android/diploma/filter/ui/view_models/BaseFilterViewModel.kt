package ru.practicum.android.diploma.filter.ui.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.Logger
import ru.practicum.android.diploma.filter.domain.api.FilterInteractor
import ru.practicum.android.diploma.filter.ui.models.BaseFilterScreenState
import ru.practicum.android.diploma.root.BaseViewModel
import javax.inject.Inject

class BaseFilterViewModel @Inject constructor(
    val filterInteractor: FilterInteractor,
    logger: Logger
) : BaseViewModel(logger) {
    private val _uiState: MutableStateFlow<BaseFilterScreenState> =
        MutableStateFlow(BaseFilterScreenState.Empty)
    val uiState: StateFlow<BaseFilterScreenState> = _uiState

    fun setApplyScreenState() {
     _uiState.value = BaseFilterScreenState.Apply
    }

    fun setEmptyScreenState() {
        _uiState.value = BaseFilterScreenState.Empty
    }

    fun getCountries(){
      viewModelScope.launch(Dispatchers.IO) {
     filterInteractor
                .getCountries()
                .collect { result ->
                 result
                }
        }
    }

}