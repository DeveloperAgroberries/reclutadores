package com.agroberriesmx.reclutadores.data.network.response

import com.google.gson.annotations.SerializedName

data class OrganigramaResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val response: List<ReclutadorRemoteModel>
)

data class ReclutadorRemoteModel(
    @SerializedName("cCodigoOrg") val cCodigoOrg: String,
    @SerializedName("vNombreOrg") val vNombreOrg: String
)