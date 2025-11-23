package com.example.androidprogram.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                _state.update { s ->
                    val sorted = applySort(list, s.sort)
                    s.copy(cards = sorted)
                }
            }
        }
    }

    fun dispatch(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.q) }
                viewModelScope.launch {
                    repo.search(intent.q).collectLatest { list ->
                        _state.update { s ->
                            val favs = list.filter { c -> c.favorite }
                            s.copy(cards = applySort(favs, s.sort))
                        }
                    }
                }
            }
            FavoritesIntent.ToggleManaging -> {
                _state.update { it.copy(managing = !it.managing, selected = emptySet()) }
            }
            is FavoritesIntent.ToggleSelect -> {
                _state.update { s ->
                    val set = s.selected.toMutableSet()
                    if (set.contains(intent.id)) set.remove(intent.id) else set.add(intent.id)
                    s.copy(selected = set)
                }
            }
            FavoritesIntent.SelectAll -> {
                _state.update { s -> s.copy(selected = s.cards.map { it.id }.toSet()) }
            }
            FavoritesIntent.ClearSelection -> {
                _state.update { it.copy(selected = emptySet()) }
            }
            FavoritesIntent.UnfavoriteSelected -> {
                val ids = _state.value.selected
                viewModelScope.launch {
                    _state.value.cards.filter { ids.contains(it.id) }.forEach { c -> repo.update(c.copy(favorite = false)) }
                    _state.update { it.copy(selected = emptySet()) }
                }
            }
            FavoritesIntent.DeleteSelected -> {
                val ids = _state.value.selected
                viewModelScope.launch {
                    ids.forEach { id -> repo.delete(id) }
                    _state.update { it.copy(selected = emptySet()) }
                }
            }
            is FavoritesIntent.SortChanged -> {
                _state.update { s -> s.copy(sort = intent.sort, cards = applySort(s.cards, intent.sort)) }
            }
        }
    }

    private fun applySort(list: List<com.example.androidprogram.model.Card>, sort: String): List<com.example.androidprogram.model.Card> = when (sort) {
        "name" -> list.sortedBy { it.name }
        "company" -> list.sortedBy { it.company ?: "" }
        "category" -> list.sortedWith(compareBy({ it.category ?: "" }, { -it.createdAt }))
        else -> list.sortedByDescending { it.createdAt }
    }
}
