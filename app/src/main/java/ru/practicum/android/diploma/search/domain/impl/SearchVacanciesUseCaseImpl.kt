package ru.practicum.android.diploma.search.domain.impl

import ru.practicum.android.diploma.Logger
import ru.practicum.android.diploma.di.annotations.NewResponse
import ru.practicum.android.diploma.search.domain.api.SearchRepository
import ru.practicum.android.diploma.search.domain.api.SearchVacanciesUseCase
import ru.practicum.android.diploma.search.domain.models.Vacancies
import ru.practicum.android.diploma.util.functional.Either
import ru.practicum.android.diploma.util.functional.Failure
import javax.inject.Inject

class SearchVacanciesUseCaseImpl @Inject constructor(
    private val repository: SearchRepository
):
    SearchVacanciesUseCase {

    @NewResponse
    override suspend fun searchVacancies(query: String): Either<Failure, Vacancies> {
        return repository.searchVacancies(query)
    }
}