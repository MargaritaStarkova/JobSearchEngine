package ru.practicum.android.diploma.search.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.practicum.android.diploma.search.data.network.dto.CountryDto
import ru.practicum.android.diploma.search.data.network.dto.response.VacanciesSearchCodeResponse

interface HhApiService {
    @GET("/vacancies")
    suspend fun search(
        @Query("text") text: String): Response<VacanciesSearchCodeResponse>

    @GET("/areas/countries")
    suspend fun getCountries(): Response<List<CountryDto>>

}



