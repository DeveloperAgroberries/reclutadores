package com.agroberriesmx.reclutadores.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FormattedCandidateModel(
    val vNomreclutador: String, // Coincide con el JSON
    val vLorigen: String,       // Coincide con el JSON
    val vNomcandidato: String,  // Coincide con el JSON

    // Documentación
    val vInedoc: String?,       // Coincide con el JSON
    val cCurp: String,          // Coincide con el JSON
    val cRfc: String,           // Coincide con el JSON
    val cActa: String,          // Coincide con el JSON
    val cNss: String,           // Coincide con el JSON
    val cBanco: String,         // Coincide con el JSON
    val cSf: String,            // Coincide con el JSON

    // Tiempos (asegúrate de que dFecharegistro sea compatible con DateTime)
    val dFecharegistro: String, // Coincide con el JSON (puedes usar Instant o Date si quieres un tipo de fecha)
    // dFechacreacion NO debe enviarse, ya que la DB lo genera.
    // Si lo envías, la DB lo ignorará si tiene DEFAULT GETDATE(), pero es mejor no enviarlo.
    // Elimina dFechacreacion si ya tienes DEFAULT GETDATE() en la DB.

    val cPagado: String // Añadir esta propiedad
) : Parcelable