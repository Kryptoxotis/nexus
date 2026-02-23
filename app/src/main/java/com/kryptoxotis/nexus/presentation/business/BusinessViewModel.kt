package com.kryptoxotis.nexus.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.repository.BusinessPassRepository
import com.kryptoxotis.nexus.data.repository.OrganizationRepository
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.EnrollmentMode
import com.kryptoxotis.nexus.domain.model.Organization
import com.kryptoxotis.nexus.domain.model.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessViewModel(
    private val passRepository: BusinessPassRepository,
    private val orgRepository: OrganizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BusinessUiState>(BusinessUiState.Idle)
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    private val _myOrganization = MutableStateFlow<Organization?>(null)
    val myOrganization: StateFlow<Organization?> = _myOrganization.asStateFlow()

    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    private val _userPasses = MutableStateFlow<List<BusinessPass>>(emptyList())
    val userPasses: StateFlow<List<BusinessPass>> = _userPasses.asStateFlow()

    private val _orgMembers = MutableStateFlow<List<BusinessPass>>(emptyList())
    val orgMembers: StateFlow<List<BusinessPass>> = _orgMembers.asStateFlow()

    init {
        viewModelScope.launch {
            passRepository.observeUserPasses().collect { passes ->
                _userPasses.value = passes
            }
        }

        // Auto-dismiss success/error states after 3 seconds
        viewModelScope.launch {
            _uiState.collect { state ->
                if (state is BusinessUiState.Success || state is BusinessUiState.Error) {
                    delay(3000)
                    if (_uiState.value == state) {
                        _uiState.value = BusinessUiState.Idle
                    }
                }
            }
        }
    }

    fun loadMyOrganization() {
        viewModelScope.launch {
            when (val result = orgRepository.getMyOrganization()) {
                is Result.Success -> {
                    _myOrganization.value = result.data
                    result.data?.id?.let { loadOrgMembers(it) }
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(result.message)
                }
            }
        }
    }

    fun loadOrganizations() {
        viewModelScope.launch {
            when (val result = orgRepository.getOrganizations()) {
                is Result.Success -> _organizations.value = result.data
                is Result.Error -> _uiState.value = BusinessUiState.Error(result.message)
            }
        }
    }

    fun loadOrgMembers(orgId: String) {
        viewModelScope.launch {
            passRepository.observeOrganizationPasses(orgId).collect { passes ->
                _orgMembers.value = passes
            }
        }
    }

    fun createOrganization(
        name: String,
        type: String?,
        description: String?,
        enrollmentMode: EnrollmentMode
    ) {
        if (name.isBlank()) {
            _uiState.value = BusinessUiState.Error("Organization name is required")
            return
        }
        if (name.length > 100) {
            _uiState.value = BusinessUiState.Error("Organization name must be under 100 characters")
            return
        }

        _uiState.value = BusinessUiState.Loading
        viewModelScope.launch {
            when (val result = orgRepository.createOrganization(name, type, description, enrollmentMode)) {
                is Result.Success -> {
                    _myOrganization.value = result.data
                    _uiState.value = BusinessUiState.Success("Organization created")
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(result.message)
                }
            }
        }
    }

    fun updateOrganization(
        orgId: String,
        name: String? = null,
        description: String? = null,
        enrollmentMode: EnrollmentMode? = null,
        staticPin: String? = null,
        allowSelfEnrollment: Boolean? = null
    ) {
        _uiState.value = BusinessUiState.Loading
        viewModelScope.launch {
            when (val result = orgRepository.updateOrganization(
                orgId, name, description, enrollmentMode, staticPin, allowSelfEnrollment
            )) {
                is Result.Success -> {
                    _uiState.value = BusinessUiState.Success("Organization updated")
                    loadMyOrganization()
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(result.message)
                }
            }
        }
    }

    fun enrollInOrganization(orgId: String, orgName: String?) {
        _uiState.value = BusinessUiState.Loading
        viewModelScope.launch {
            when (val result = passRepository.enrollInOrganization(orgId, orgName)) {
                is Result.Success -> {
                    _uiState.value = BusinessUiState.Success("Enrolled successfully")
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(result.message)
                }
            }
        }
    }

    fun enrollWithPin(orgId: String, orgName: String?, pin: String) {
        _uiState.value = BusinessUiState.Loading
        viewModelScope.launch {
            when (val pinResult = orgRepository.validateEnrollmentPin(orgId, pin)) {
                is Result.Success -> {
                    if (pinResult.data) {
                        enrollInOrganization(orgId, orgName)
                    } else {
                        _uiState.value = BusinessUiState.Error("Invalid PIN")
                    }
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(pinResult.message)
                }
            }
        }
    }

    fun createEnrollmentPin(orgId: String, pinCode: String) {
        _uiState.value = BusinessUiState.Loading
        viewModelScope.launch {
            when (val result = orgRepository.createEnrollmentPin(orgId, pinCode)) {
                is Result.Success -> {
                    _uiState.value = BusinessUiState.Success("PIN created")
                }
                is Result.Error -> {
                    _uiState.value = BusinessUiState.Error(result.message)
                }
            }
        }
    }

    fun syncPasses() {
        viewModelScope.launch {
            passRepository.syncFromSupabase()
        }
    }

    fun resetState() {
        _uiState.value = BusinessUiState.Idle
    }
}

sealed class BusinessUiState {
    object Idle : BusinessUiState()
    object Loading : BusinessUiState()
    data class Success(val message: String) : BusinessUiState()
    data class Error(val message: String) : BusinessUiState()
}
