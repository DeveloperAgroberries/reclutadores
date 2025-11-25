package com.agroberriesmx.reclutadores.ui.privacypolicy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.usecase.LoginsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    private val getLoginsUseCase: LoginsUseCase,
    //private val getRouteUseCase: RouteUseCase,
    //private val getVehicleUseCase: VehicleUseCase,
    //private val getWorkerUseCase: WorkerUseCase
) : ViewModel() {
    private var _state = MutableLiveData<PrivacyPolicyState>()
    val state: LiveData<PrivacyPolicyState> = _state

    lateinit var loginModel: LoginModel

    fun dataResponse(token: String) {
        viewModelScope.launch {
            _state.value = PrivacyPolicyState.Loading
            try {
                val responseLogins = getLoginsUseCase(token)
                //val responseRoutes = getRouteUseCase()
                //val responseVehicles = getVehicleUseCase()
                //val responseWorkers = getWorkerUseCase()

                if (responseLogins != null /*&& responseRoutes != null && responseVehicles != null && responseWorkers != null*/) {
                    loginModel = responseLogins
                    //routeList = responseRoutes
                    //routeList = responseRoutes ?: emptyList()
                    //vehicleList = responseVehicles
                    //vehicleList = responseVehicles ?: emptyList()
                    //workerModel = responseWorkers
                    //workerList = responseWorkers ?: emptyList()

                    _state.value = PrivacyPolicyState.Success(loginModel, /*routeList, vehicleList, workerList*/)
                } else {
                    _state.value = PrivacyPolicyState.Error("Alguno de los datos fallo en la descarga, vuelve a intentarlo.")
                }
            } catch (e: Exception) {
                _state.value = PrivacyPolicyState.Error(e.message ?: "A ocurrido un error")
            }
        }
    }
}