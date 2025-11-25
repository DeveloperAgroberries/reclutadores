package com.agroberriesmx.reclutadores.data.network

import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.data.network.request.UploadResponse
import com.agroberriesmx.reclutadores.data.network.response.LoginResponse
import com.agroberriesmx.reclutadores.data.network.response.LoginsResponse
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ReclutadoresApiService {
    @POST("LoginUser")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("ListLogins")
    suspend fun loginsData(@Header("Authorization") token: String): LoginsResponse

    @POST("InsertarCandidato")
    suspend fun uploadData(@Body records: List<FormattedCandidateModel>): Response<UploadResponse>
}
