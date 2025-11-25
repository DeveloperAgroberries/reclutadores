package com.agroberriesmx.reclutadores.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.reclutadores.data.network.request.LoginRequest
import com.agroberriesmx.reclutadores.data.repository.LocalAuthRepository // ⬅️ Necesario
import com.agroberriesmx.reclutadores.domain.model.TokenModel
import com.agroberriesmx.reclutadores.domain.usecase.LoginUseCase // ⬅️ Necesario
import com.agroberriesmx.reclutadores.utils.NetworkUtils // ⬅️ Necesario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getLoginUseCase: LoginUseCase,
    private val networkUtils: NetworkUtils,
    private val localAuthRepository: LocalAuthRepository
) : ViewModel() {

    private var _state = MutableLiveData<LoginState>(LoginState.Waiting)
    val state: LiveData<LoginState> = _state

    lateinit var tokenModel: TokenModel

    fun login(userId: String, password: String, activeUser: String, creatorId: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading

            val user = userId.trim()
            val pass = password

            // 1. Verificar red
            if (networkUtils.isNetworkAvailable()) {
                // --- INTENTO ONLINE ---
                try {
                    val loginRequest = LoginRequest(user, pass, activeUser, creatorId)
                    val response = getLoginUseCase(loginRequest) // Llamada a la API

                    //LOG para ver la respuesta del servidor Ricardo Dimas 23-10-2025
                    Log.e("LoginViewModel", "Respuesta_SERVER: $response")

                    if (response != null) {
                        // Éxito Online: Guardamos credenciales para futuro offline
                        localAuthRepository.saveCredentials(user, pass)
                        tokenModel = response
                        _state.value = LoginState.Success(response, isLocal = false)
                        return@launch
                    }
                    // Si el servidor responde con un error o token nulo, intenta fallback local
                    attemptLocalLogin(user, pass, "Credenciales inválidas.")

                } catch (e: Exception) {
                    // Si falla la red (Timeout) o la API lanza excepción (401, 500)
                    attemptLocalLogin(user, pass, "Fallo de conexión o servidor: ${e.message}")
                }
            } else {
                // --- SIN CONEXIÓN: INTENTO OFFLINE DIRECTO ---
                attemptLocalLogin(user, pass, "No hay conexión a internet.")
            }
        }
    }

    private fun attemptLocalLogin(userId: String, password: String, onlineErrorMessage: String) {
        // 2. Intentar validar credenciales contra el almacenamiento local
        if (localAuthRepository.validateCredentials(userId, password)) {
            // Éxito Offline
            _state.value = LoginState.Success(null, isLocal = true) // Token es null en offline
        } else {
            // Fallo definitivo: No hay conexión Y las credenciales locales no son válidas.
            _state.value = LoginState.Error("Acceso denegado o falla en el servidor. ${onlineErrorMessage}.")
        }
    }
}