package com.agroberriesmx.reclutadores.domain

import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel

interface CredentialsRepository {

    suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?
    suspend fun insertUsers(users: List<LoginModel>): List<Long?>
    suspend fun deleteAllUsers()

    //SINCRONIZACION DE CANDIDATOS
    suspend fun getUnsynchronizedRecords(): List<RecordModel>?
    suspend fun updateCandidateRecord(record: RecordModel): Int?
    //suspend fun listUnsynchronizedRecords(): List<RecordModel>?

}