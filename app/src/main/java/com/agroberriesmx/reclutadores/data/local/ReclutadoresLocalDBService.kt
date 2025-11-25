package com.agroberriesmx.reclutadores.data.local

import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel

interface ReclutadoresLocalDBService {
    //Credentials
    suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?
    suspend fun getAllUsers(): List<LoginModel>
    suspend fun insertUsers(users: List<LoginModel>): List<Long?>
    suspend fun deleteAllUsers()
    suspend fun getUnsynchronizedRecords(): List<RecordModel>?
    suspend fun updateCandidateRecord(
        controlLog: Int, // <-- El ID local de la tabla zCandidatos
        nReclutador: String,
        lOrigen: String,
        nCandidato: String,
        ineDoc: String?,
        dCurp: String,
        dRFC: String,
        dActa: String,
        dNSS: String,
        cBanco: String,
        cSF: String,
        dFechaRegistro: String,
        dFechaCreacion: String,
        cPagado: String,
        isDocUploaded: Int,   // Nuevo campo
        docServerUrl: String?, // Nuevo campo
        isSynced: Int         // Campo de sincronización
    ): Int?

}