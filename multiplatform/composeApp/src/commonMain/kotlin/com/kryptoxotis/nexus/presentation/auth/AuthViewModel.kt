package com.kryptoxotis.nexus.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.remote.AuthManager
import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "Nexus:AuthVM"
        private const val POLL_INTERVAL_MS = 30_000L
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<AuthManager.SavedAccount>>(emptyList())
    val savedAccounts: StateFlow<List<AuthManager.SavedAccount>> = _savedAccounts.asStateFlow()

    private val _businessRequest = MutableStateFlow<BusinessRequestDto?>(null)
    val businessRequest: StateFlow<BusinessRequestDto?> = _businessRequest.asStateFlow()

    private var pollingJob: Job? = null

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

    fun signInWithIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.signInWithIdToken(idToken)
            result.fold(
                onSuccess = { loadProfile() },
                onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "Sign-in failed") }
            )
        }
    }

    private suspend fun loadProfile() {
        var profile = authManager.getCurrentProfile()
        if (profile == null) {
            profile = authManager.createProfileIfMissing()
        }
        _profile.value = profile

        if (profile == null) {
            authManager.signOut()
            _authState.value = AuthState.NotAllowed
        } else {
            val accountType = AccountType.fromString(profile.accountType)
            _authState.value = AuthState.Authenticated(
                userId = authManager.getCurrentUserId() ?: "",
                accountType = accountType
            )
            if (accountType == AccountType.INDIVIDUAL) {
                _businessRequest.value = authManager.getBusinessRequestStatus()
                if (_businessRequest.value?.status == "pending") startBusinessRequestPolling()
            } else {
                stopBusinessRequestPolling()
            }
        }
    }

    private fun startBusinessRequestPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            var attempts = 0
            while (attempts < 100) {
                attempts++
                delay(POLL_INTERVAL_MS)
                try {
                    val request = authManager.getBusinessRequestStatus()
                    _businessRequest.value = request
                    if (request?.status == "approved") { loadProfile(); break }
                    if (request?.status == "rejected") break
                } catch (e: Exception) {
                    Logger.e(TAG, "Polling error", e)
                }
            }
        }
    }

    private fun stopBusinessRequestPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun setAccountType(type: AccountType) {
        viewModelScope.launch {
            authManager.updateAccountType(type)
            val userId = authManager.getCurrentUserId() ?: ""
            _profile.value = _profile.value?.copy(accountType = type.toDbString())
            _authState.value = AuthState.Authenticated(userId = userId, accountType = type)
        }
    }

    fun requestBusinessUpgrade(businessName: String, businessType: String?, contactEmail: String?, message: String?) {
        viewModelScope.launch {
            val result = authManager.submitBusinessRequest(businessName, businessType, contactEmail, message)
            if (result is Result.Success) {
                _businessRequest.value = authManager.getBusinessRequestStatus()
                startBusinessRequestPolling()
            }
        }
    }

    fun switchAccount(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authManager.switchAccount(email)
            if (success) loadProfile() else _authState.value = AuthState.Error("Failed to switch account")
        }
    }

    fun signOut() {
        stopBusinessRequestPolling()
        viewModelScope.launch {
            authManager.signOut()
            val remaining = authManager.getSavedAccounts()
            if (remaining.isEmpty()) {
                _authState.value = AuthState.NotAuthenticated
                _profile.value = null
                _businessRequest.value = null
            } else {
                loadProfile()
            }
        }
    }

    fun getCurrentUserId(): String? = authManager.getCurrentUserId()

    fun resetError() { _authState.value = AuthState.NotAuthenticated }

    override fun onCleared() {
        super.onCleared()
        stopBusinessRequestPolling()
    }
}
