package com.agroberriesmx.reclutadores.data.network.response

import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.google.gson.annotations.SerializedName

data class LoginsResponse (
    @SerializedName("vNombreUsu") val vNombreUsu:String,
    @SerializedName("cCodigoUsu") val cCodigoUsu:String,
    @SerializedName("vPasswordUsu") val vPasswordUsu:String
){
    fun toDomain(): LoginModel{
        return LoginModel(
            vNombreUsu = vNombreUsu,
            cCodigoUsu = cCodigoUsu,
            vPasswordUsu = vPasswordUsu
        )
    }
}