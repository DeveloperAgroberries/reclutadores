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
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
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

                if (!localData.isNullOrEmpty()) {
                    // 1. Transformación de datos (Manteniendo tu lógica de fechas)
                    val transformedData = localData.map { register ->
                        val mexicoTimeZone = TimeZone.getTimeZone("America/Mexico_City")
                        val calendar = Calendar.getInstance(mexicoTimeZone)
                        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
                        isoFormatter.timeZone = mexicoTimeZone
                        val dFechaRegistroActual = isoFormatter.format(calendar.time)

                        FormattedCandidateModel(
                            vNomreclutador = register.nReclutador.trim(),
                            vLorigen = register.lOrigen,
                            vNomcandidato = register.nCandidato.trim(),
                            vInedoc = register.ineDoc, // Sigue siendo la ruta local para el JSON
                            cCurp = register.dCurp,
                            cRfc = register.dRFC,
                            cActa = register.dActa,
                            cNss = register.dNSS,
                            cBanco = register.cBanco,
                            cSf = register.cSF,
                            dFecharegistro = register.dFechaRegistro ?: dFechaRegistroActual,
                            cPagado = "0"
                        )
                    }

                    // 2. Convertir lista de datos a RequestBody (JSON)
                    val jsonString = Gson().toJson(transformedData)
                    val candidatosPart = RequestBody.create("text/plain".toMediaTypeOrNull(), jsonString)

                    // 3. Convertir rutas de archivos a MultipartBody.Part
                    val imageParts = localData.map { register ->
                        val file = File(register.ineDoc)
                        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                        // "archivosIne" debe coincidir con el nombre en el API C#
                        MultipartBody.Part.createFormData("archivosIne", file.name, requestFile)
                    }

                    // 4. Llamada al UseCase
                    val response = uploadUseCase(candidatosPart, imageParts)

                    // 5. Manejo de resultados
                    if (response == "Ok") {
                        localData.forEach { record ->
                            record.isSynced = 1
                            recordsRepository.updateCandidateRecord(record)
                        }
                        _state.value = SynchronizeState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRecords()
                    } else {
                        when (response) {
                            "Unauthorized" -> _state.value = SynchronizeState.Error("No cuentas con un token...")
                            "Not Found" -> _state.value = SynchronizeState.Error("Servicio no disponible...")
                            else -> _state.value = SynchronizeState.Error(response)
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