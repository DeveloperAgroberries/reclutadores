package com.agroberriesmx.reclutadores.domain.usecase

import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.domain.Repository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(
        loginRequest: LoginRequest
    ) = repository.getToken(loginRequest)
}