package com.agroberriesmx.reclutadores.data.network.response

import com.agroberriesmx.reclutadores.domain.model.TokenModel
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String
) {
    fun toDomain(): TokenModel {
        return TokenModel(
            token = token
        )
    }
}