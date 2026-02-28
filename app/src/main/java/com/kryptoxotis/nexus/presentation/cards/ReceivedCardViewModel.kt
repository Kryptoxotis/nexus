package com.kryptoxotis.nexus.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.local.ReceivedCardEntity
import com.kryptoxotis.nexus.data.repository.ReceivedCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReceivedCardViewModel(
    private val repository: ReceivedCardRepository
) : ViewModel() {

    val contacts: StateFlow<List<ReceivedCardEntity>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getContact(id: String): Flow<ReceivedCardEntity?> = flow {
        emit(repository.getById(id))
    }

    fun saveContact(
        name: String,
        jobTitle: String = "",
        company: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        linkedin: String = "",
        instagram: String = "",
        twitter: String = "",
        github: String = ""
    ) {
        viewModelScope.launch {
            repository.saveContact(
                name = name, jobTitle = jobTitle, company = company,
                phone = phone, email = email, website = website,
                linkedin = linkedin, instagram = instagram,
                twitter = twitter, github = github
            )
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }
}
