package com.agroberriesmx.reclutadores.ui.synchronize

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.reclutadores.data.local.ReclutadoresLocalDBService
import com.agroberriesmx.reclutadores.domain.CredentialsRepository
import com.agroberriesmx.reclutadores.domain.model.FormattedCandidateModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel
import com.agroberriesmx.reclutadores.domain.usecase.UploadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class SynchronizeViewModel @Inject constructor(
    private val databaseService: ReclutadoresLocalDBService,
    private val recordsRepository: CredentialsRepository,
    private val uploadUseCase: UploadUseCase
) : ViewModel() {

    private val _state = MutableLiveData<SynchronizeState>(SynchronizeState.Waiting)
    val state: LiveData<SynchronizeState> get() = _state

    private val _pendingRecords = MutableLiveData<List<RecordModel>>()
    val pendingRecords: LiveData<List<RecordModel>> get() = _pendingRecords

    fun synchronize() {
        viewModelScope.launch {
            _state.value = SynchronizeState.Loading
            try {
                //workerUseCase.invoke()
                //routeUseCase.invoke()
                //vehicleUseCase.invoke()
                //_state.value = SynchronizeState.CatalogSuccess
            } catch (e: Exception) {
                _state.value = SynchronizeState.Error("Ha ocurrido un error al sincronizar catálogos")
            }
        }
    }

    fun loadPendingRecords() {
        viewModelScope.launch {
            val records = recordsRepository.getUnsynchronizedRecords()
            _pendingRecords.value = records ?: emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun upload() {
        viewModelScope.launch {
            _state.value = SynchronizeState.Loading
            try {
                val localData = recordsRepository.getUnsynchronizedRecords()
                Log.d("SynchronizeViewModel", "Local data to upload: $localData")
                if (localData != null && localData.isNotEmpty()) {
                    val transformedData: List<FormattedCandidateModel> = localData.map { register ->

                        // 1. Obtener la hora actual usando la zona horaria de México
                        val mexicoTimeZone = TimeZone.getTimeZone("America/Mexico_City")
                        val calendar = Calendar.getInstance(mexicoTimeZone)
                        val localTimeInMexico = calendar.time

                        // 2. Definir el formateador de salida con el patrón exacto, pero SIN la 'Z'
                        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())

                        // 3. CRÍTICO: Forzar al formateador a usar la hora de MÉXICO para que imprima 14:29.
                        // NO USAR UTC.
                        isoFormatter.timeZone = mexicoTimeZone

                        // 4. Formatear la hora
                        val dFechaRegistro = isoFormatter.format(localTimeInMexico)

                        FormattedCandidateModel(
                            vNomreclutador = register.nReclutador.trim(), // Ajusta si 'register' ya tiene 'vNomreclutador'
                            vLorigen = register.lOrigen,                 // Ajusta si 'register' ya tiene 'vLorigen'
                            vNomcandidato = register.nCandidato.trim(),  // Ajusta si 'register' ya tiene 'vNomcandidato'

                            vInedoc = register.ineDoc,
                            cCurp = register.dCurp,
                            cRfc = register.dRFC,
                            cActa = register.dActa,
                            cNss = register.dNSS,
                            cBanco = register.cBanco,
                            cSf = register.cSF,

                            dFecharegistro = register.dFechaRegistro, // Asegúrate de que este String ya esté en formato ISO 8601 con 'Z'

                            cPagado = "0"
                        )
                    }

                    val response = uploadUseCase(transformedData)

                    if (response == "Ok") {
                        localData.forEach { record ->
                            record.isSynced = 1
                            recordsRepository.updateCandidateRecord(record)
                        }
                        _state.value = SynchronizeState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRecords()
                    } else {
                        if (response == "Unauthorized") {
                            _state.value = SynchronizeState.Error("No cuentas con un token para enviar los datos, cierra e inicia sesion y vuelve a intentarlo, por favor.")
                        } else if(response == "Not Found"){
                            _state.value = SynchronizeState.Error("Servicio no disponible, por favor intenta mas tarde.")
                        } else {
                            _state.value = SynchronizeState.Error(response)
                        }
                    }
                } else {
                    _state.value = SynchronizeState.Error("No hay nada que enviar")
                }
            } catch (e: Exception) {
                _state.value = SynchronizeState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    fun clearState() {
        _state.value = SynchronizeState.Waiting
    }
}