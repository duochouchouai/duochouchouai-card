package com.example.androidprogram.feature.cards.list

sealed class CardListIntent {
    data class QueryChanged(val query: String) : CardListIntent()
    data class Delete(val id: Long) : CardListIntent()
    data class ToggleFavorite(val id: Long, val favorite: Boolean) : CardListIntent()
    object Refresh : CardListIntent()
    data class SortChanged(val sort: String) : CardListIntent()
}
