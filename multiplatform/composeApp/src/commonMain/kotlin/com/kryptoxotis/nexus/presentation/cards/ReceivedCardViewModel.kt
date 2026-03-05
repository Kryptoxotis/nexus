package com.kryptoxotis.nexus.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.local.ReceivedCard
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

    val contacts: StateFlow<List<ReceivedCard>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getContact(id: String): Flow<ReceivedCard?> = flow {
        emit(repository.getById(id))
    }

    fun saveContact(
        name: String, jobTitle: String = "", company: String = "",
        phone: String = "", email: String = "", website: String = "",
        linkedin: String = "", instagram: String = "", twitter: String = "",
        github: String = "", facebook: String = "", youtube: String = "",
        tiktok: String = "", discord: String = "", twitch: String = "",
        whatsapp: String = ""
    ) {
        viewModelScope.launch {
            repository.saveContact(
                name = name, jobTitle = jobTitle, company = company,
                phone = phone, email = email, website = website,
                linkedin = linkedin, instagram = instagram,
                twitter = twitter, github = github,
                facebook = facebook, youtube = youtube,
                tiktok = tiktok, discord = discord,
                twitch = twitch, whatsapp = whatsapp
            )
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch { repository.deleteContact(id) }
    }
}
