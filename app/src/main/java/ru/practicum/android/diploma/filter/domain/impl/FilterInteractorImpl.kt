package ru.practicum.android.diploma.filter.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.filter.domain.api.FilterInteractor
import ru.practicum.android.diploma.filter.domain.api.FilterRepository
import ru.practicum.android.diploma.search.data.network.dto.CountryDto
import ru.practicum.android.diploma.search.domain.api.SearchRepository
import javax.inject.Inject

class FilterInteractorImpl @Inject constructor
    (
    val filterRepository: FilterRepository,
    val searchRepository: SearchRepository,
) :
    FilterInteractor {
    override fun filter() {

    }

    override suspend fun getCountries(): Flow<CountryDto> {
     return   searchRepository.getCountries()
    }


}