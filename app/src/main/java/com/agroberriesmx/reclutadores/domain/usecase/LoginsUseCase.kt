package com.agroberriesmx.reclutadores.domain.usecase

import com.agroberriesmx.reclutadores.domain.Repository
import javax.inject.Inject

class LoginsUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(token: String) = repository.getLogins(token)
}