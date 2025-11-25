package com.agroberriesmx.reclutadores.data

import com.agroberriesmx.reclutadores.data.local.ReclutadoresLocalDBService
import com.agroberriesmx.reclutadores.domain.CredentialsRepository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel
import javax.inject.Inject

class CredentialsRepositoryImpl @Inject constructor(
    private val localDBService: ReclutadoresLocalDBService
) : CredentialsRepository {
    override suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel? {
        return localDBService.getUserByCodeAndPassword(cUsu, vPassword)
    }

    override suspend fun insertUsers(users: List<LoginModel>): List<Long?> {
        return localDBService.insertUsers(users)
    }

    override suspend fun deleteAllUsers() {}

    override suspend fun getUnsynchronizedRecords(): List<RecordModel>? {
        return localDBService.getUnsynchronizedRecords()
    }

    // 🚀 NUEVA FUNCIÓN PARA ACTUALIZAR REGISTROS DE CANDIDATOS
    override suspend fun updateCandidateRecord(candidate: RecordModel): Int? {
        return localDBService.updateCandidateRecord(
            controlLog = candidate.controlLog.toInt(),
            nReclutador = candidate.nReclutador,
            lOrigen = candidate.lOrigen,
            nCandidato = candidate.nCandidato,
            ineDoc = candidate.ineDoc,
            dCurp = candidate.dCurp,
            dRFC = candidate.dRFC,
            dActa = candidate.dActa,
            dNSS = candidate.dNSS,
            cBanco = candidate.cBanco,
            cSF = candidate.cSF,
            dFechaRegistro = candidate.dFechaRegistro,
            dFechaCreacion = candidate.dFechaCreacion,
            cPagado = candidate.cPagado,
            isDocUploaded = candidate.isDocUploaded, // Asegúrate de que tu FormattedCandidateModel tenga este campo
            docServerUrl = candidate.docServerUrl,   // Asegúrate de que tu FormattedCandidateModel tenga este campo
            isSynced = candidate.isSynced
        )
    }
}