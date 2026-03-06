package com.agroberriesmx.reclutadores.data.network

import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.data.network.request.UploadResponse
import com.agroberriesmx.reclutadores.data.network.response.LoginResponse
import com.agroberriesmx.reclutadores.data.network.response.LoginsResponse
import com.agroberriesmx.reclutadores.data.network.response.OrganigramaResponse
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ReclutadoresApiService {
    @POST("LoginUser")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("ListLogins")
    suspend fun loginsData(@Header("Authorization") token: String): LoginsResponse

    @Multipart
    @POST("InsertarCandidato")
    suspend fun uploadData(
        @Part("candidatos") candidatos: RequestBody,
        @Part archivosIne: List<MultipartBody.Part>
    ): Response<UploadResponse>

    @GET("ListReclutadores")
    suspend fun getReclutadores(): Response<OrganigramaResponse>
}
