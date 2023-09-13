package ru.practicum.android.diploma.details.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.Logger
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.details.domain.DetailsInteractor
import ru.practicum.android.diploma.details.domain.models.VacancyFullInfo
import ru.practicum.android.diploma.root.BaseViewModel
import ru.practicum.android.diploma.sharing.domain.api.SharingInteractor
import ru.practicum.android.diploma.util.functional.Failure
import ru.practicum.android.diploma.util.thisName

class DetailsViewModel @AssistedInject constructor(
    logger: Logger,
    private val detailsInteractor: DetailsInteractor,
    private val sharingInteractor: SharingInteractor,
    @Assisted("vacancyId")
    private val vacancyId: String
) : BaseViewModel(logger) {

    private val _uiState = MutableStateFlow<DetailsScreenState>(DetailsScreenState.Default)
    val uiState: StateFlow<DetailsScreenState> = _uiState

    private var vacancy: VacancyFullInfo? = null
    private var isInFavorites = false

    fun handleAddToFavsButton() {
        val message = when (isInFavorites) {
            true -> {
                deleteVacancy()
                "vacancy removed from favs"
            }

            else -> {
                vacancy?.let { addToFavorites(it) }
                "vacancy added to favs"
            }
        }
        log(thisName, "handleAddToFavsButton $message")
    }

    private fun addToFavorites(vacancy: VacancyFullInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            detailsInteractor.addVacancyToFavorites(vacancy).collect {
                log(thisName, "${vacancy.id} inserted")
            }
        }
    }

    private fun deleteVacancy() {
        viewModelScope.launch(Dispatchers.IO) {
            detailsInteractor.removeVacancyFromFavorite(vacancyId).collect {
                log(thisName, "$vacancyId deleted")
            }
        }
    }

    fun showIfInFavouriteById() {
        viewModelScope.launch(Dispatchers.IO) {
            detailsInteractor.showIfInFavourite(vacancyId).collect { vacancy ->
                log(thisName, "getFavoriteVacancyById -> $vacancy")
                isInFavorites = vacancy
                if (vacancy) {
                    _uiState.value = DetailsScreenState.AddAnimation
                } else {
                    _uiState.value = DetailsScreenState.DeleteAnimation
                }
            }
        }
    }

    fun sendVacancy() {
        log(thisName, "sendVacancy()")
        _uiState.value.let { state ->
            if (state is DetailsScreenState.Content) {
                state.vacancy.alternateUrl
                sharingInteractor.sendVacancy(state.vacancy.alternateUrl)
            } else {
                return
            }
        }
    }

    fun writeEmail(context: Context) {
        log(thisName, "writeEmail()")
        _uiState.value.let { state ->
            if (state is DetailsScreenState.Content &&
                state.vacancy.contactEmail != context.getString(R.string.no_info)
            ) {
                sharingInteractor.writeEmail(state.vacancy.contactEmail)
            } else {
                return
            }
        }
    }

    fun makeCall() {
        log(thisName, "makeCall()")
        _uiState.value.let { state ->
            if (state is DetailsScreenState.Content &&
                state.vacancy.contactPhones.isNotEmpty()
            ) {
                sharingInteractor.makeCall(state.vacancy.contactPhones[0])
            } else {
                return
            }
        }
    }

    fun getVacancyByID() {
        _uiState.value = DetailsScreenState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            detailsInteractor.getFullVacancyInfoById(vacancyId).fold(
                ::handleFailure,
                ::handleSuccess
            )
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        if (failure is Failure.Offline) _uiState.value = DetailsScreenState.Offline(failure)
        else _uiState.value = DetailsScreenState.Error(failure)
    }

    private fun handleSuccess(vacancy: VacancyFullInfo) {
        log(thisName, "handleSuccess -> $vacancy")
        _uiState.value = DetailsScreenState.Content(vacancy)
        this.vacancy = vacancy
    }

    @AssistedFactory
    interface Factory{
        fun create(@Assisted("vacancyId") vacancyId: String): DetailsViewModel
    }
    companion object{

        @Suppress("UNCHECKED_CAST")
        fun provideDetailsViewModelFactory(factory: Factory, vacancyId: String): ViewModelProvider.Factory{
            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(vacancyId) as T
                }
            }
        }
    }
}