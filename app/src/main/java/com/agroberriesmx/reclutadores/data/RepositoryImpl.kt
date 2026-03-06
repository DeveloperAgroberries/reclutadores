package com.agroberriesmx.reclutadores.data

import android.util.Log
import com.agroberriesmx.reclutadores.data.local.DatabaseHelper
import com.agroberriesmx.reclutadores.data.network.ReclutadoresApiService
import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel
import com.agroberriesmx.reclutadores.domain.model.TokenModel
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.google.gson.Gson

class RepositoryImpl @Inject constructor(
    private val apiService: ReclutadoresApiService,
    private val dbHelper: DatabaseHelper // 👈 Inyectamos tu base de datos
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
            // 1. Convertimos la lista de objetos a JSON String
            val jsonString = Gson().toJson(records)
            val candidatosPart = RequestBody.create("text/plain".toMediaTypeOrNull(), jsonString)

            // 2. Creamos la lista de archivos MultipartBody.Part
            val imageParts = records.map { candidate ->
                val file = File(candidate.vInedoc) // vInedoc tiene la ruta local
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)

                // El nombre "archivosIne" debe ser igual al del parámetro en C#
                MultipartBody.Part.createFormData("archivosIne", file.name, requestFile)
            }

            // 3. Llamamos al API con los DOS parámetros requeridos
            val response = apiService.uploadData(candidatosPart, imageParts)

            if (response.isSuccessful) {
                // Ajusta 'mensaje' o 'message' según cómo se llame en tu UploadResponse de C#
                Pair(response.body()?.message ?: "Ok", response.code())
            } else {
                Pair(response.message(), response.code())
            }
        } catch (e: Exception) {
            // Loguea el error para depurar si falla la lectura del archivo
            Log.e("UploadError", "Error al procesar archivos: ${e.message}")
            Pair(e.message, null)
        }
    }

    override suspend fun getReclutadores(token: String): List<ReclutadoresModel> {
        return try {
            val response = apiService.getReclutadores() // 🔍 ¿Aquí falta pasar el token?

            if (response.isSuccessful && response.body() != null) {
                val listaReclutadores = response.body()!!.response.map { item ->
                    ReclutadoresModel(cCodigoOrg = item.cCodigoOrg, vNombreOrg = item.vNombreOrg)
                }
                Log.d("REPO_DEBUG", "API éxito: ${listaReclutadores.size} elementos")
                dbHelper.saveReclutadoresLocal(listaReclutadores)
                listaReclutadores
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("REPO_DEBUG", "API Error: ${response.code()} - $errorBody")
                dbHelper.getAllReclutadoresLocal()
            }
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "Fallo total: ${e.message}")
            dbHelper.getAllReclutadoresLocal()
        }
    }
}