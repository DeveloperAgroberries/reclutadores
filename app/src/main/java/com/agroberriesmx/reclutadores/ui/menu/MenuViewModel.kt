package com.agroberriesmx.reclutadores.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroberriesmx.reclutadores.domain.Repository
import com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    // Estado que el Fragment va a observar
    private val _reclutadores = MutableStateFlow<List<ReclutadoresModel>>(emptyList())
    val reclutadores: StateFlow<List<ReclutadoresModel>> = _reclutadores

    fun fetchReclutadores(token: String) {
        viewModelScope.launch {
            // Llamamos al repositorio (que ya maneja API + DB Local)
            val lista = repository.getReclutadores(token)
            _reclutadores.value = lista
        }
    }
}