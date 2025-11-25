package com.agroberriesmx.reclutadores.ui.privacypolicy

import com.agroberriesmx.reclutadores.domain.model.LoginModel

sealed class PrivacyPolicyState {
    data object Waiting:PrivacyPolicyState()
    data object Loading:PrivacyPolicyState()
    data class Error(val message: String):PrivacyPolicyState()
    data class Success(val logins: LoginModel):PrivacyPolicyState()
}