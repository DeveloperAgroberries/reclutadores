package com.agroberriesmx.reclutadores.ui.synchronize

sealed class SynchronizeState {
    object Waiting : SynchronizeState()
    object Loading : SynchronizeState()
    data class UploadSuccess(val message: String) : SynchronizeState()
    data class Error(val message: String) : SynchronizeState()
}