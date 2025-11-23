package com.example.androidprogram.feature.cards.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardListViewModel(private val repo: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(CardListState())
    val state: StateFlow<CardListState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            repo.getAll().collectLatest { list ->
                _state.update { it.copy(loading = false, cards = list) }
            }
        }
    }

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow.debounce(150).flatMapLatest { q -> repo.search(q) }.collectLatest { list ->
                _state.update { it.copy(cards = list) }
            }
        }
    }

    fun dispatch(intent: CardListIntent) {
        when (intent) {
            is CardListIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.query) }
                queryFlow.value = intent.query
            }
            is CardListIntent.Delete -> {
                viewModelScope.launch { repo.delete(intent.id) }
            }
            is CardListIntent.ToggleFavorite -> {
                val card = _state.value.cards.find { it.id == intent.id } ?: return
                viewModelScope.launch { repo.update(card.copy(favorite = intent.favorite)) }
            }
            CardListIntent.Refresh -> load()
        }
    }
}
