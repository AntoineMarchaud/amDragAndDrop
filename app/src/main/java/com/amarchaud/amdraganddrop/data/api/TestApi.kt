package com.amarchaud.amdraganddrop.data.api

import com.amarchaud.amdraganddrop.domain.ApiOnePerson
import retrofit2.Call
import retrofit2.http.GET

interface TestApi {
    @GET("mojo/team.json")
    fun getProfiles(): Call<List<ApiOnePerson>>
}