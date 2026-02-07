package com.nfcpass.presentation.passes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcpass.data.repository.PassRepository
import com.nfcpass.domain.model.Pass
import com.nfcpass.domain.model.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for pass management screens.
 */
class PassViewModel(
    private val repository: PassRepository
) : ViewModel() {

    private val _passes = MutableStateFlow<List<Pass>>(emptyList())
    val passes: StateFlow<List<Pass>> = _passes.asStateFlow()

    private val _activePass = MutableStateFlow<Pass?>(null)
    val activePass: StateFlow<Pass?> = _activePass.asStateFlow()

    private val _uiState = MutableStateFlow<PassUiState>(PassUiState.Idle)
    val uiState: StateFlow<PassUiState> = _uiState.asStateFlow()

    private var passesJob: Job? = null
    private var activePassJob: Job? = null

    init {
        startObserving()
    }

    /**
     * Re-subscribes to pass flows with the current user ID.
     * Call this after auth state changes (login, logout, account switch).
     */
    fun refreshPasses() {
        startObserving()
    }

    private fun startObserving() {
        passesJob?.cancel()
        activePassJob?.cancel()

        passesJob = viewModelScope.launch {
            repository.observeUserPasses().collect { passList ->
                _passes.value = passList
            }
        }

        activePassJob = viewModelScope.launch {
            repository.observeActivePass().collect { pass ->
                _activePass.value = pass
            }
        }
    }

    /**
     * Adds a new pass
     */
    fun addPass(passId: String, passName: String, organization: String, expiryDate: String?, link: String? = null) {
        // Validate input
        if (passId.isBlank()) {
            _uiState.value = PassUiState.Error("Pass ID cannot be empty")
            return
        }

        if (passName.isBlank()) {
            _uiState.value = PassUiState.Error("Pass name cannot be empty")
            return
        }

        if (organization.isBlank()) {
            _uiState.value = PassUiState.Error("Organization cannot be empty")
            return
        }

        _uiState.value = PassUiState.Loading

        viewModelScope.launch {
            when (val result = repository.addPass(passId, passName, organization, expiryDate, link)) {
                is Result.Success -> {
                    _uiState.value = PassUiState.Success("Pass added successfully")
                }
                is Result.Error -> {
                    _uiState.value = PassUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Activates a pass for NFC emulation
     */
    fun activatePass(passId: String) {
        _uiState.value = PassUiState.Loading

        viewModelScope.launch {
            when (val result = repository.activatePass(passId)) {
                is Result.Success -> {
                    _uiState.value = PassUiState.Success("Pass activated")
                }
                is Result.Error -> {
                    _uiState.value = PassUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Deactivates a pass
     */
    fun deactivatePass(passId: String) {
        _uiState.value = PassUiState.Loading

        viewModelScope.launch {
            when (val result = repository.deactivatePass(passId)) {
                is Result.Success -> {
                    _uiState.value = PassUiState.Success("Pass deactivated")
                }
                is Result.Error -> {
                    _uiState.value = PassUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Deletes a pass
     */
    fun deletePass(passId: String) {
        _uiState.value = PassUiState.Loading

        viewModelScope.launch {
            when (val result = repository.deletePass(passId)) {
                is Result.Success -> {
                    _uiState.value = PassUiState.Success("Pass deleted")
                }
                is Result.Error -> {
                    _uiState.value = PassUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Resets UI state to Idle
     */
    fun resetState() {
        _uiState.value = PassUiState.Idle
    }
}

/**
 * Sealed class representing pass UI states
 */
sealed class PassUiState {
    object Idle : PassUiState()
    object Loading : PassUiState()
    data class Success(val message: String) : PassUiState()
    data class Error(val message: String) : PassUiState()
}
