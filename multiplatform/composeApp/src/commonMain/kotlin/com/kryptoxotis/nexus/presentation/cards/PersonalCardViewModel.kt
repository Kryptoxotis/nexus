package com.kryptoxotis.nexus.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.repository.FileRepository
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.NfcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CardUiState {
    data object Idle : CardUiState()
    data object Loading : CardUiState()
    data class Success(val message: String) : CardUiState()
    data class Error(val message: String) : CardUiState()
    data class FileUploaded(val url: String) : CardUiState()
}

class PersonalCardViewModel(
    private val cardRepository: PersonalCardRepository,
    private val fileRepository: FileRepository,
    private val nfcManager: NfcManager? = null
) : ViewModel() {

    private val _cards = MutableStateFlow<List<PersonalCard>>(emptyList())
    val cards: StateFlow<List<PersonalCard>> = _cards.asStateFlow()

    private val _activeCard = MutableStateFlow<PersonalCard?>(null)
    val activeCard: StateFlow<PersonalCard?> = _activeCard.asStateFlow()

    private val _uiState = MutableStateFlow<CardUiState>(CardUiState.Idle)
    val uiState: StateFlow<CardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { cardRepository.observeUserCards().collect { _cards.value = it } }
        viewModelScope.launch { cardRepository.observeActiveCard().collect { _activeCard.value = it } }
        viewModelScope.launch {
            _uiState.collect { state ->
                if (state is CardUiState.Success || state is CardUiState.Error) {
                    delay(3000)
                    if (_uiState.value == state) _uiState.value = CardUiState.Idle
                }
            }
        }
    }

    fun addCard(cardType: CardType, title: String, content: String? = null, icon: String? = null,
                color: String? = null, imageUrl: String? = null, cardShape: String = "card") {
        if (title.isBlank()) { _uiState.value = CardUiState.Error("Title cannot be empty"); return }
        if (title.length > 200) { _uiState.value = CardUiState.Error("Title must be under 200 characters"); return }
        _uiState.value = CardUiState.Loading
        viewModelScope.launch {
            when (val result = cardRepository.addCard(cardType, title, content, icon, color, imageUrl, cardShape)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card added")
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun activateCard(cardId: String) {
        viewModelScope.launch {
            when (val result = cardRepository.activateCard(cardId)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card activated for NFC")
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun activateCardWithOverride(cardId: String, isUri: Boolean, nfcContent: String) {
        viewModelScope.launch {
            when (val result = cardRepository.activateCardOnly(cardId)) {
                is Result.Success -> {
                    delay(100)
                    nfcManager?.writeNdefCache(nfcContent, isUri)
                    _uiState.value = CardUiState.Success("Ready to share via NFC")
                }
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun deactivateCard(cardId: String) {
        viewModelScope.launch {
            when (val result = cardRepository.deactivateCard(cardId)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card deactivated")
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun updateCard(cardId: String, title: String, content: String?, color: String?, cardShape: String) {
        if (title.isBlank()) { _uiState.value = CardUiState.Error("Title cannot be empty"); return }
        viewModelScope.launch {
            when (val result = cardRepository.updateCard(cardId, title, content, color, cardShape)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card updated")
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            when (val result = cardRepository.deleteCard(cardId)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card deleted")
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun reorderCards(cardIds: List<String>) { viewModelScope.launch { cardRepository.reorderCards(cardIds) } }
    fun createStack(cardId1: String, cardId2: String) { viewModelScope.launch { cardRepository.createStack(cardId1, cardId2) } }
    fun addToStack(cardId: String, stackId: String) { viewModelScope.launch { cardRepository.addToStack(cardId, stackId) } }
    fun removeFromStack(cardId: String) { viewModelScope.launch { cardRepository.removeFromStack(cardId) } }
    fun dissolveStack(stackId: String) { viewModelScope.launch { cardRepository.dissolveStack(stackId) } }

    fun uploadFile(fileBytes: ByteArray, filename: String, mimeType: String?) {
        _uiState.value = CardUiState.Loading
        viewModelScope.launch {
            when (val result = fileRepository.uploadFile(fileBytes, filename, mimeType)) {
                is Result.Success -> _uiState.value = CardUiState.FileUploaded(result.data)
                is Result.Error -> _uiState.value = CardUiState.Error(result.message)
            }
        }
    }

    fun refreshCards() { viewModelScope.launch { cardRepository.syncFromSupabase() } }
    fun resetState() { _uiState.value = CardUiState.Idle }
    fun setError(message: String) { _uiState.value = CardUiState.Error(message) }
}
