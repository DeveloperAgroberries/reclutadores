package com.agroberriesmx.reclutadores.domain.usecase

import android.util.Log
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import javax.inject.Inject

class UploadUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke (records: List<FormattedCandidateModel>):String {
        val response = repository.uploadRecords(records)
        //Log.d("RecordsImp", "Datos que se van a enviar: $records") // <-- ¡Añade esta línea!
        return when (response.second) {
            200 -> "Ok"
            401 -> "Unauthorized"
            404 -> "Not Found"
            else -> response.first ?: "Error desconocido"
        }
    }
}