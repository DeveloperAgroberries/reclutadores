package com.agroberriesmx.reclutadores.domain.usecase

import android.util.Log
import com.agroberriesmx.reclutadores.data.network.ReclutadoresApiService
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

// AGREGAMOS @Inject constructor AQUÍ:
class UploadUseCase @Inject constructor(
    private val apiService: ReclutadoresApiService
) {

    suspend operator fun invoke(
        candidatos: RequestBody,
        imagenes: List<MultipartBody.Part>
    ): String {
        return try {
            val response = apiService.uploadData(candidatos, imagenes)

            if (response.isSuccessful) {
                // OJO: Verifica si en tu C# pusiste "mensaje" o "message"
                // Si en el API pusiste return Ok(new { mensaje = "Ok" }), usa .mensaje
                if (response.body()?.message == "Ok") "Ok" else "Error en respuesta"
            } else {
                when (response.code()) {
                    401 -> "Unauthorized"
                    404 -> "Not Found"
                    else -> "Error: ${response.code()}"
                }
            }
        } catch (e: Exception) {
            e.message ?: "Error de conexión"
        }
    }
}