package com.example.androidprogram.feature.cards.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CardListViewModel(private val repo: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(CardListState())
    val state: StateFlow<CardListState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            repo.getAll().distinctUntilChanged().collectLatest { list ->
                _state.update { s -> s.copy(loading = false, cards = applySort(list, s.sort)) }
            }
        }
    }

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(150)
                .flatMapLatest { q ->
                    val flow = if (q.isBlank()) repo.getAll() else repo.search(q)
                    flow.distinctUntilChanged()
                }
                .collectLatest { list ->
                    _state.update { s -> s.copy(cards = applySort(list, s.sort)) }
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
            is CardListIntent.SortChanged -> {
                _state.update { s -> s.copy(sort = intent.sort, cards = applySort(s.cards, intent.sort)) }
            }
        }
    }

    private fun applySort(list: List<Card>, sort: String): List<Card> = when (sort) {
        "name" -> list.sortedBy { it.name }
        "company" -> list.sortedBy { it.company ?: "" }
        "category" -> list.sortedWith(compareBy({ it.category ?: "" }, { -it.createdAt }))
        else -> list.sortedByDescending { it.createdAt }
    }
}
