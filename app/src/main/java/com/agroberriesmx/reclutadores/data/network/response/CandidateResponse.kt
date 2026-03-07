package com.agroberriesmx.reclutadores.data.network.response

import com.google.gson.annotations.SerializedName

// Este es el objeto principal que recibe la respuesta del API
data class CandidateResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val response: List<CandidateRemote>
)

data class CandidateRemote(
    @SerializedName("iIdcandidato") val iIdcandidato: Int,
    @SerializedName("vNomcandidato") val vNomcandidato: String?,
    @SerializedName("vLorigen") val vLorigen: String?,
    @SerializedName("vNomreclutador") val vNomreclutador: String?,
    @SerializedName("vInedoc") val vInedoc: String?,
    @SerializedName("cCurp") val cCurp: String?,     // 👈 Estas son las que faltaban
    @SerializedName("cRfc") val cRfc: String?,       // 👈 Agregada
    @SerializedName("cActa") val cActa: String?,     // 👈 Agregada
    @SerializedName("cNss") val cNss: String?,       // 👈 Agregada
    @SerializedName("cBanco") val cBanco: String?,   // 👈 Agregada
    @SerializedName("cSf") val cSf: String?,         // 👈 Agregada
    @SerializedName("cPagado") val cPagado: String?  // 👈 Agregada
)