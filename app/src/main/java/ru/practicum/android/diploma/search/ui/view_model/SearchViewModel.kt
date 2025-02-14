package ru.practicum.android.diploma.search.ui.view_model

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.Logger
import ru.practicum.android.diploma.filter.domain.api.FilterInteractor
import ru.practicum.android.diploma.filter.domain.models.SelectedFilter
import ru.practicum.android.diploma.filter.ui.view_models.BaseFilterViewModel
import ru.practicum.android.diploma.root.BaseViewModel
import ru.practicum.android.diploma.search.domain.api.SearchVacanciesUseCase
import ru.practicum.android.diploma.search.domain.models.Vacancies
import ru.practicum.android.diploma.search.domain.models.Vacancy
import ru.practicum.android.diploma.search.ui.models.SearchUiState
import ru.practicum.android.diploma.util.delayedAction
import ru.practicum.android.diploma.util.functional.Failure
import ru.practicum.android.diploma.util.thisName
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val searchVacanciesUseCase: SearchVacanciesUseCase,
    private val filterInteractor: FilterInteractor,
    private val logger: Logger,
) : BaseViewModel(logger) {
    
    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState.Default)
    val uiState: StateFlow<SearchUiState> = _uiState
    
    private var latestSearchQuery: String? = null
    private var vacancyList: List<Vacancy> = emptyList()
    private var searchJob: Job? = null
    private var isNextPageLoading: Boolean = false
    private var found: Int = FIRST_PAGE
    private var maxPages: Int = FIRST_PAGE
    private var currentPage: Int = FIRST_PAGE
    private var selectedFilter: SelectedFilter = SelectedFilter.empty
    
    private val onSearchDebounce =
        delayedAction<String>(coroutineScope = viewModelScope, action = { query ->
            logger.log(thisName, "+++onSearchDebounce+++ -> searchVacancies()")
            searchVacancies(query = query, filter = selectedFilter, isFirstPage = currentPage == FIRST_PAGE)
        })
    
    fun isFilterSelected(): Boolean = selectedFilter != SelectedFilter.empty
    
    fun onSearchQueryChanged(query: String) {
        logger.log(thisName, "+++onSearchQueryChanged+++ -> $query: String")
        if (query == latestSearchQuery) return
        latestSearchQuery = query
        resetPagesState()
        
        if (query.isEmpty()) {
            searchJob?.cancel()
            onSearchDebounce("")
            _uiState.value = SearchUiState.Default
        } else {
            onSearchDebounce(query)
        }
    }
    
    fun searchVacancies(query: String, filter: SelectedFilter? = null, isFirstPage: Boolean) {
        if (query.isNotEmpty()) {
            logger.log(thisName, "+++searchVacancies+++ -> $query: String, $filter: SelectedFilter, $isFirstPage: Boolean")
            if (isFirstPage) _uiState.value = SearchUiState.Loading
            searchJob = viewModelScope.launch(Dispatchers.IO) {
                searchVacanciesUseCase(query, currentPage, filter).fold(
                    ::handleFailure,
                    ::handleSuccess
                )
            }
        }
    }
    
    fun onScrolledBottom() {
        logger.log(thisName, "+++onScrolledBottom+++")
        if (!isNextPageLoading && currentPage < maxPages) {
            logger.log(thisName, "+++onScrolledBottom+++ -> currentPage = $currentPage, maxPages = $maxPages")
            isNextPageLoading = true
            latestSearchQuery?.let {
                searchVacancies(
                    query = it, filter = selectedFilter, isFirstPage = currentPage == FIRST_PAGE
                )
            }
        }
    }
    
    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        logger.log(thisName, "+++handleFailure+++ -> maxPages = $maxPages, currentPage = $currentPage")
        if (currentPage == maxPages) _uiState.value = SearchUiState.Error(failure)
        else _uiState.value = SearchUiState.ErrorScrollLoading(failure)
        isNextPageLoading = false
    }
    
    private fun handleSuccess(vacancies: Vacancies) {
        currentPage++
        maxPages = vacancies.pages
        found = vacancies.found
        vacancyList += vacancies.items
        
        logger.log(thisName, "+++handleSuccess+++ -> maxPages = $maxPages, found = $found, vacancyList = $vacancyList")
        
        _uiState.value = SearchUiState.Content(
            list = vacancyList, found = vacancies.found, isLastPage = currentPage >= maxPages
        )
        isNextPageLoading = false
    }
    
    fun fillFilterData() {
        viewModelScope.launch {
            val newSelectedFilter = filterInteractor.getSavedFilterSettings(BaseFilterViewModel.FILTER_KEY)
            if (newSelectedFilter != selectedFilter) {
                selectedFilter = newSelectedFilter
                resetPagesState()
                log(thisName, "fillFilterData = $selectedFilter")
                latestSearchQuery?.let {
                    searchVacancies(
                        query = it, filter = selectedFilter, isFirstPage = currentPage == FIRST_PAGE
                    )
                }
            }
        }
    }

    fun getFilterSettings(): SelectedFilter {
        logger.log(thisName, "+++getFilterSettings+++")
        return selectedFilter
    }
    
    private fun resetPagesState() {
        logger.log(thisName, "+++resetPagesState+++")
        currentPage = FIRST_PAGE
        maxPages = FIRST_PAGE
        vacancyList = emptyList()
    }
    
    companion object {
        private const val FIRST_PAGE = 0
    }
}
