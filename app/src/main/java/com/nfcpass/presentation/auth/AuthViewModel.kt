package com.nfcpass.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcpass.data.remote.AuthManager
import com.nfcpass.data.remote.dto.ProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<AuthManager.SavedAccount>>(emptyList())
    val savedAccounts: StateFlow<List<AuthManager.SavedAccount>> = _savedAccounts.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.observeSavedAccounts().collect { accounts ->
                _savedAccounts.value = accounts
            }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val restored = authManager.restoreSession()
            if (restored) {
                loadProfile()
            } else {
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.signInWithGoogle(activity)
            result.fold(
                onSuccess = { loadProfile() },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
                }
            )
        }
    }

    private suspend fun loadProfile() {
        val profile = authManager.getCurrentProfile()
        _profile.value = profile

        if (profile == null || profile.currentRole == null) {
            _authState.value = AuthState.NeedsProfileSetup
        } else {
            _authState.value = AuthState.Authenticated(
                userId = authManager.getCurrentUserId() ?: "",
                role = profile.currentRole
            )
        }
    }

    fun setRole(role: String) {
        viewModelScope.launch {
            authManager.updateProfileRole(role)
            val userId = authManager.getCurrentUserId() ?: ""
            _profile.value = _profile.value?.copy(currentRole = role)
            _authState.value = AuthState.Authenticated(userId = userId, role = role)
        }
    }

    fun switchAccount(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authManager.switchAccount(email)
            if (success) {
                loadProfile()
            } else {
                _authState.value = AuthState.Error("Failed to switch account")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            val remaining = authManager.getSavedAccounts()
            if (remaining.isEmpty()) {
                _authState.value = AuthState.NotAuthenticated
                _profile.value = null
            } else {
                loadProfile()
            }
        }
    }

    fun getCurrentUserId(): String? = authManager.getCurrentUserId()

    fun resetError() {
        _authState.value = AuthState.NotAuthenticated
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    object NeedsProfileSetup : AuthState()
    data class Authenticated(val userId: String, val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
