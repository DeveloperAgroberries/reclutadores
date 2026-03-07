package com.agroberriesmx.reclutadores.ui.candidates

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.reclutadores.data.network.ReclutadoresApiService
import com.agroberriesmx.reclutadores.data.network.response.CandidateRemote
import com.agroberriesmx.reclutadores.data.repository.LocalAuthRepository
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CandidatesViewModel @Inject constructor(
    private val apiService: ReclutadoresApiService,
    private val repository: Repository,
    private val localAuthRepository: LocalAuthRepository // 👈 Inyectado del Login
) : ViewModel() {

    private val _reclutadores = MutableStateFlow<List<ReclutadoresModel>>(emptyList())
    val reclutadores: StateFlow<List<ReclutadoresModel>> = _reclutadores

    // 👈 Declaramos esta variable para que ya no te dé error de "Unresolved reference"
    val reclutadorNombre = MutableStateFlow("")
    val candidates = MutableStateFlow<List<CandidateRemote>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Dentro de initData en CandidatesViewModel.kt
    fun initData(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Obtenemos el usuario
                val userId = localAuthRepository.getSavedUserId()

                // Cambiamos el "!userId.isNullOrEmpty()" por una forma más segura:
                if (!userId.isNullOrBlank()) {
                    val lista = repository.getReclutadores(userId)
                    _reclutadores.value = lista

                    // Dentro de initData en CandidatesViewModel.kt
                    if (lista.isNotEmpty()) {
                        // IMPORTANTE: Aquí armamos el formato "ID - Nombre" para la búsqueda inicial automática
                        val reclutadorFormateado = "${lista[0].cCodigoOrg} - ${lista[0].vNombreOrg}"

                        // Actualizamos el texto en el dropdown (opcional, para que el usuario vea qué se buscó)
                        reclutadorNombre.value = reclutadorFormateado

                        // ❌ BORRA O COMENTA ESTA LÍNEA:
                        // fetchCandidates(reclutadorFormateado, context)
                    }
                } else {
                    errorMessage.value = "No se encontró sesión de usuario activa."
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al inicializar datos: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchCandidates(reclutador: String, context: Context) {
        if (!isNetworkAvailable(context)) {
            errorMessage.value = "Sin conexión a internet"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val response = apiService.consultarCandidatos(reclutador)

                // --- AGREGAMOS ESTO PARA DEPURAR ---
                android.util.Log.d("API_DEBUG", "URL llamada con: $reclutador")

                if (response.isSuccessful) {
                    val lista = response.body()?.response ?: emptyList()
                    candidates.value = lista
                    if (lista.isEmpty()) errorMessage.value = "No hay candidatos registrados"
                }
            } catch (e: Exception) {
                errorMessage.value = "Fallo de conexión: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}