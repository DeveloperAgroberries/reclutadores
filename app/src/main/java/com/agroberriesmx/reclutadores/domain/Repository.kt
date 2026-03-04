package com.agroberriesmx.reclutadores.domain

import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel
import com.agroberriesmx.reclutadores.domain.model.TokenModel

interface Repository {
    //suspend fun getToken(loginRequest: LoginRequest): Result<TokenModel>
    suspend fun getToken(loginRequest: LoginRequest): TokenModel?
    suspend fun getLogins(token: String): LoginModel?
    suspend fun uploadRecords(records: List<FormattedCandidateModel>): Pair<String?, Int?>
    // Asegúrate de que devuelva List<ReclutadoresModel>
    suspend fun getReclutadores(token: String): List<ReclutadoresModel>
}