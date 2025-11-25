package com.agroberriesmx.reclutadores.data

import android.util.Log
import com.agroberriesmx.reclutadores.data.network.ReclutadoresApiService
import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.TokenModel
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val apiService: ReclutadoresApiService
) : Repository {
    companion object {
        private const val APP_INFO_TAG_KEY = "ControlReclutadores"
    }

    override suspend fun getToken(loginRequest: LoginRequest): TokenModel? {
        runCatching { apiService.login(loginRequest) }
            .onSuccess { return it.toDomain() }
            .onFailure { Log.i(APP_INFO_TAG_KEY, "Ha ocurrido un error ${it.message}") }

        return null
    }

    override suspend fun getLogins(token: String): LoginModel? {
        runCatching { apiService.loginsData(token) }
            .onSuccess { return it.toDomain() }
            .onFailure { Log.i(APP_INFO_TAG_KEY, "Ha ocurrido un error ${it.message}") }

        return null
    }

    override suspend fun uploadRecords(records: List<FormattedCandidateModel>): Pair<String?, Int?> {
        return try {
            val response = apiService.uploadData(records)

            if (response.isSuccessful) {
                Pair(response.body()?.message, response.code()) // O el formato de respuesta que manejes
            } else {
                Pair(response.message(), response.code())
            }
        } catch (e: Exception) {
            // Maneja los errores de red o excepciones
            Pair(e.message, null)
        }
    }
}