package com.kryptoxotis.nexus.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.repository.FileRepository
import com.kryptoxotis.nexus.data.repository.PersonalCardRepository
import com.kryptoxotis.nexus.domain.model.CardType
import com.kryptoxotis.nexus.domain.model.PersonalCard
import com.kryptoxotis.nexus.domain.model.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalCardViewModel(
    private val cardRepository: PersonalCardRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<PersonalCard>>(emptyList())
    val cards: StateFlow<List<PersonalCard>> = _cards.asStateFlow()

    private val _activeCard = MutableStateFlow<PersonalCard?>(null)
    val activeCard: StateFlow<PersonalCard?> = _activeCard.asStateFlow()

    private val _uiState = MutableStateFlow<CardUiState>(CardUiState.Idle)
    val uiState: StateFlow<CardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cardRepository.observeUserCards().collect { cardList ->
                _cards.value = cardList
            }
        }

        viewModelScope.launch {
            cardRepository.observeActiveCard().collect { card ->
                _activeCard.value = card
            }
        }

        // Auto-dismiss success/error states after 3 seconds
        viewModelScope.launch {
            _uiState.collect { state ->
                if (state is CardUiState.Success || state is CardUiState.Error) {
                    delay(3000)
                    if (_uiState.value == state) {
                        _uiState.value = CardUiState.Idle
                    }
                }
            }
        }
    }

    fun addCard(
        cardType: CardType,
        title: String,
        content: String? = null,
        icon: String? = null,
        color: String? = null,
        imageUrl: String? = null
    ) {
        if (title.isBlank()) {
            _uiState.value = CardUiState.Error("Title cannot be empty")
            return
        }
        if (title.length > 200) {
            _uiState.value = CardUiState.Error("Title must be under 200 characters")
            return
        }

        _uiState.value = CardUiState.Loading

        viewModelScope.launch {
            when (val result = cardRepository.addCard(cardType, title, content, icon, color, imageUrl)) {
                is Result.Success -> {
                    _uiState.value = CardUiState.Success("Card added")
                }
                is Result.Error -> {
                    _uiState.value = CardUiState.Error(result.message)
                }
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

    fun deactivateCard(cardId: String) {
        viewModelScope.launch {
            when (val result = cardRepository.deactivateCard(cardId)) {
                is Result.Success -> _uiState.value = CardUiState.Success("Card deactivated")
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

    fun reorderCards(cardIds: List<String>) {
        viewModelScope.launch {
            cardRepository.reorderCards(cardIds)
        }
    }

    fun uploadFile(fileBytes: ByteArray, filename: String, mimeType: String?) {
        _uiState.value = CardUiState.Loading
        viewModelScope.launch {
            when (val result = fileRepository.uploadFile(fileBytes, filename, mimeType)) {
                is Result.Success -> {
                    _uiState.value = CardUiState.FileUploaded(result.data)
                }
                is Result.Error -> {
                    _uiState.value = CardUiState.Error(result.message)
                }
            }
        }
    }

    fun refreshCards() {
        viewModelScope.launch {
            cardRepository.syncFromSupabase()
        }
    }

    fun resetState() {
        _uiState.value = CardUiState.Idle
    }

}

sealed class CardUiState {
    object Idle : CardUiState()
    object Loading : CardUiState()
    data class Success(val message: String) : CardUiState()
    data class Error(val message: String) : CardUiState()
    data class FileUploaded(val url: String) : CardUiState()
}
