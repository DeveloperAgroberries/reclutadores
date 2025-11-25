package com.agroberriesmx.reclutadores.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUtils @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Verifica si el dispositivo tiene una conexión de red activa (Wi-Fi, Datos Móviles o Ethernet).
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Obtener la red activa
        val network = connectivityManager.activeNetwork ?: return false

        // Obtener las capacidades de la red activa
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Retorna true si tiene alguna de estas capacidades de transporte
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}