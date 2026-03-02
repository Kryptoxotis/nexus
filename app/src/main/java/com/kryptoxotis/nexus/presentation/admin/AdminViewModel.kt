package com.kryptoxotis.nexus.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.remote.dto.AllowedEmailDto
import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.data.repository.AdminRepository
import com.kryptoxotis.nexus.domain.model.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var autoDismissJob: Job? = null

    private fun scheduleAutoDismiss(isError: Boolean = false) {
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            delay(if (isError) 6000L else 3000L)
            _uiState.value = AdminUiState.Idle
        }
    }

    private val _pendingRequests = MutableStateFlow<List<BusinessRequestDto>>(emptyList())
    val pendingRequests: StateFlow<List<BusinessRequestDto>> = _pendingRequests.asStateFlow()

    private val _users = MutableStateFlow<List<ProfileDto>>(emptyList())
    val users: StateFlow<List<ProfileDto>> = _users.asStateFlow()

    private val _organizations = MutableStateFlow<List<OrganizationDto>>(emptyList())
    val organizations: StateFlow<List<OrganizationDto>> = _organizations.asStateFlow()

    private val _allowedEmails = MutableStateFlow<List<AllowedEmailDto>>(emptyList())
    val allowedEmails: StateFlow<List<AllowedEmailDto>> = _allowedEmails.asStateFlow()

    private fun handleResult(result: Result<String>, onSuccess: () -> Unit = {}) {
        when (result) {
            is Result.Success -> {
                _uiState.value = AdminUiState.Success(result.data)
                scheduleAutoDismiss(isError = false)
                onSuccess()
            }
            is Result.Error -> {
                _uiState.value = AdminUiState.Error(result.message)
                scheduleAutoDismiss(isError = true)
            }
        }
    }

    fun loadPendingRequests() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            when (val result = adminRepository.loadPendingRequests()) {
                is Result.Success -> {
                    _pendingRequests.value = result.data
                    _uiState.value = AdminUiState.Idle
                }
                is Result.Error -> _uiState.value = AdminUiState.Error(result.message)
            }
        }
    }

    fun approveRequest(request: BusinessRequestDto) {
        if (request.id.isNullOrBlank()) {
            _uiState.value = AdminUiState.Error("Request ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.approveRequest(request)) { loadPendingRequests() }
        }
    }

    fun rejectRequest(requestId: String) {
        if (requestId.isBlank()) {
            _uiState.value = AdminUiState.Error("Request ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.rejectRequest(requestId)) { loadPendingRequests() }
        }
    }

    fun loadUsers() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            when (val result = adminRepository.loadUsers()) {
                is Result.Success -> {
                    _users.value = result.data
                    _uiState.value = AdminUiState.Idle
                }
                is Result.Error -> _uiState.value = AdminUiState.Error(result.message)
            }
        }
    }

    fun updateUserStatus(userId: String, status: String) {
        if (userId.isBlank()) {
            _uiState.value = AdminUiState.Error("User ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.updateUserStatus(userId, status)) { loadUsers() }
        }
    }

    fun changeAccountType(userId: String, newType: String) {
        if (userId.isBlank()) {
            _uiState.value = AdminUiState.Error("User ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.changeAccountType(userId, newType)) { loadUsers() }
        }
    }

    fun loadAllowedEmails() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            when (val result = adminRepository.loadAllowedEmails()) {
                is Result.Success -> {
                    _allowedEmails.value = result.data
                    _uiState.value = AdminUiState.Idle
                }
                is Result.Error -> _uiState.value = AdminUiState.Error(result.message)
            }
        }
    }

    fun createUser(email: String, fullName: String, accountType: String) {
        if (email.isBlank()) {
            _uiState.value = AdminUiState.Error("Invalid email address")
            return
        }
        if (fullName.isBlank() || fullName.length > 200) {
            _uiState.value = AdminUiState.Error("Name must be 1-200 characters")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.createUser(email, fullName, accountType)) {
                loadUsers()
                loadAllowedEmails()
            }
        }
    }

    fun deleteAllowedEmail(emailId: String) {
        if (emailId.isBlank()) {
            _uiState.value = AdminUiState.Error("Email ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.deleteAllowedEmail(emailId)) { loadAllowedEmails() }
        }
    }

    fun deleteUser(userId: String) {
        if (userId.isBlank()) {
            _uiState.value = AdminUiState.Error("User ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.deleteUser(userId)) { loadUsers() }
        }
    }

    fun loadOrganizations() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            when (val result = adminRepository.loadOrganizations()) {
                is Result.Success -> {
                    _organizations.value = result.data
                    _uiState.value = AdminUiState.Idle
                }
                is Result.Error -> _uiState.value = AdminUiState.Error(result.message)
            }
        }
    }

    fun toggleOrganizationActive(orgId: String, isActive: Boolean) {
        if (orgId.isBlank()) {
            _uiState.value = AdminUiState.Error("Organization ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.toggleOrganizationActive(orgId, isActive)) { loadOrganizations() }
        }
    }

    fun createOrganization(
        name: String,
        type: String?,
        description: String?,
        ownerId: String,
        enrollmentMode: String
    ) {
        if (name.isBlank()) {
            _uiState.value = AdminUiState.Error("Organization name is required")
            return
        }
        if (ownerId.isBlank()) {
            _uiState.value = AdminUiState.Error("Owner is required")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.createOrganization(name, type, description, ownerId, enrollmentMode)) {
                loadOrganizations()
            }
        }
    }

    fun deleteOrganization(orgId: String) {
        if (orgId.isBlank()) {
            _uiState.value = AdminUiState.Error("Organization ID is missing")
            return
        }
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            handleResult(adminRepository.deleteOrganization(orgId)) { loadOrganizations() }
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }
}

sealed class AdminUiState {
    data object Idle : AdminUiState()
    data object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}
