package com.agroberriesmx.reclutadores.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAuthRepository @Inject constructor(@ApplicationContext context: Context) {

    // Usaremos un archivo privado de SharedPreferences
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val KEY_USER = "last_user_id"
    private val KEY_PASS = "last_password"

    /**
     * Guarda el último usuario y contraseña al tener un login ONLINE exitoso.
     * Solo guarda el usuario limpio (sin trim).
     */
    fun saveCredentials(userId: String, password: String) {
        Log.d("LocalAuth", "Creedenciales: $userId, Logged in: $password")
        prefs.edit()
            .putString(KEY_USER, userId.trim()) // Limpiamos y guardamos
            .putString(KEY_PASS, password)
            .apply()
    }

    /**
     * Valida las credenciales ingresadas por el usuario contra las guardadas localmente.
     * Se usa cuando NO hay conexión a la red.
     */
    // MODIFICACIÓN TEMPORAL PARA DEPURACIÓN EN LocalAuthRepository.kt
    fun validateCredentials(userId: String, password: String): Boolean {
        val inputUser = userId.trim()
        val storedUser = prefs.getString(KEY_USER, null)
        val storedPass = prefs.getString(KEY_PASS, null)

        Log.d("AuthCheck", "--- VALIDANDO OFFLINE ---")
        Log.d("AuthCheck", "Ingresado: U=$inputUser, P=$password")
        Log.d("AuthCheck", "Guardado: U=$storedUser, P=$storedPass")

        val isValid = (inputUser == storedUser) && (password == storedPass)

        Log.d("AuthCheck", "Resultado: $isValid") // Debería ser 'true' si funciona

        return isValid
    }

    /**
     * Opcional: Para el logout.
     */
    fun clearCredentials() {
        prefs.edit().clear().apply()
    }
}