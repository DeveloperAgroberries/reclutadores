package com.agroberriesmx.reclutadores.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordModel(
    // ⭐⭐ AGREGADO: ID de la fila para UPDATE/DELETE
    val controlLog: Long = 0,

    // Datos del Reclutador (sesión)
    val nReclutador: String,
    val lOrigen: String,
    val nCandidato: String,

    // Documentación
    val ineDoc: String?,        // Ruta local del INE (puede ser nulo)
    val dCurp: String,          // ¿Tiene CURP? (SI/NO)
    val dRFC: String,
    val dActa: String,
    val dNSS: String,
    val cBanco: String,         // Certificado Bancario
    val cSF: String,            // Certificado Fiscal

    // ⭐⭐ AGREGADO: Campo de la DB que faltaba
    val cPagado: String,

    // Tiempos
    val dFechaRegistro: String, // Fecha de registro capturada en el formulario
    val dFechaCreacion: String, // Fecha de creación en la base de datos local

    // Estados de Sincronización y Subida de Documentos
    val isDocUploaded: Int = 0, // 0: Pendiente de subir la foto / 1: Subida
    val docServerUrl: String?,  // URL del documento en el servidor (puede ser nulo)
    var isSynced: Int = 0
) : Parcelable