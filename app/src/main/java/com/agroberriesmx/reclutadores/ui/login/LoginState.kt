package com.agroberriesmx.reclutadores.ui.login

import com.agroberriesmx.reclutadores.domain.model.TokenModel

/**
 * Define los posibles estados de la interfaz de usuario de la pantalla de Login.
 */
sealed class LoginState {

    /** Estado inicial, esperando la acción del usuario. */
    data object Waiting : LoginState()

    /** Estado de carga o procesamiento (ej. llamando a la API). */
    data object Loading : LoginState()

    /**
     * Estado de éxito en el login.
     * @param success El modelo de token de respuesta (será nulo si isLocal es true).
     * @param isLocal Indica si el acceso fue exitoso usando credenciales almacenadas localmente (offline).
     */
    data class Success(val success: TokenModel?, val isLocal: Boolean) : LoginState()

    /**
     * Estado de error en el login.
     * @param message Mensaje de error a mostrar al usuario.
     */
    data class Error(val message: String) : LoginState()
}