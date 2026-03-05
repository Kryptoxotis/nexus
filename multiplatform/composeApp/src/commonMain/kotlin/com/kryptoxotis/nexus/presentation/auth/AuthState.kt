package com.kryptoxotis.nexus.presentation.auth

import com.kryptoxotis.nexus.domain.model.AccountType

sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data object NotAllowed : AuthState()
    data class Authenticated(val userId: String, val accountType: AccountType) : AuthState()
    data class Error(val message: String) : AuthState()
}
