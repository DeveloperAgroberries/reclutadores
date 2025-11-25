package com.agroberriesmx.reclutadores.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel
import javax.inject.Inject

class ReclutadoresLocalDBServiceImpl @Inject constructor(private val databaseHelper: DatabaseHelper) : ReclutadoresLocalDBService {
    //Credentials
    override suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?{
        return databaseHelper.getUserByCodeAndPassword(cUsu, vPassword)
    }

    override suspend fun getAllUsers(): List<LoginModel>{
        return databaseHelper.getAllUsers()
    }

    override suspend fun insertUsers(users: List<LoginModel>): List<Long?> {
        return users.map{
                user ->
            databaseHelper.insertUser(
                vNombreUsu = user.vNombreUsu,
                cCodigoUsu = user.cCodigoUsu,
                vPasswordUsu = user.vPasswordUsu
            )
        }
    }

    override suspend fun deleteAllUsers() {
        return databaseHelper.deleteAllUsers()
    }

    override suspend fun getUnsynchronizedRecords(): List<RecordModel>? {
        return databaseHelper.getUnsynchronizedRecords()
    }

    // 🚀 IMPLEMENTACIÓN ACTUALIZADA: Usando los parámetros de CANDIDATO
    override suspend fun updateCandidateRecord(
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
        isDocUploaded: Int,
        docServerUrl: String?,
        isSynced: Int
    ): Int? {
        // Llama a la función del DatabaseHelper con los nuevos parámetros
        return databaseHelper.updateCandidateRecord(
            controlLog,
            nReclutador,
            lOrigen,
            nCandidato,
            ineDoc,
            dCurp,
            dRFC,
            dActa,
            dNSS,
            cBanco,
            cSF,
            dFechaRegistro,
            dFechaCreacion,
            cPagado,
            isDocUploaded,
            docServerUrl,
            isSynced
        )
    }
}