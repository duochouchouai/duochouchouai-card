package com.example.androidprogram.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repo: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state

    init {
        viewModelScope.launch {
            repo.getFavorites().collectLatest { list ->
                _state.update { s -> s.copy(cards = applySort(list, s.sort)) }
            }
        }
    }

    fun dispatch(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.q) }
                viewModelScope.launch {
                    repo.search(intent.q).collectLatest { list ->
                        _state.update { s -> s.copy(cards = applySort(list.filter { c -> c.favorite }, s.sort)) }
                    }
                }
            }
            FavoritesIntent.ToggleManaging -> {
                _state.update { it.copy(managing = !it.managing, selected = emptySet()) }
            }
            is FavoritesIntent.ToggleSelect -> {
                _state.update { s ->
                    val ns = if (s.selected.contains(intent.id)) s.selected - intent.id else s.selected + intent.id
                    s.copy(selected = ns)
                }
            }
            FavoritesIntent.SelectAll -> {
                val all = _state.value.cards.map { it.id }.toSet()
                _state.update { it.copy(selected = all) }
            }
            FavoritesIntent.ClearSelection -> {
                _state.update { it.copy(selected = emptySet()) }
            }
            FavoritesIntent.UnfavoriteSelected -> {
                val ids = _state.value.selected
                if (ids.isNotEmpty()) {
                    viewModelScope.launch {
                        val current = _state.value.cards
                        ids.forEach { id ->
                            val c = current.find { it.id == id } ?: return@forEach
                            repo.update(c.copy(favorite = false))
                        }
                        _state.update { it.copy(selected = emptySet()) }
                    }
                }
            }
            FavoritesIntent.DeleteSelected -> {
                val ids = _state.value.selected
                if (ids.isNotEmpty()) {
                    viewModelScope.launch {
                        ids.forEach { id -> repo.delete(id) }
                        _state.update { it.copy(selected = emptySet()) }
                    }
                }
            }
            is FavoritesIntent.SortChanged -> {
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
