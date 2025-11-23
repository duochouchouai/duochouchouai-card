package com.example.androidprogram.feature.favorites

sealed class FavoritesIntent {
    data class QueryChanged(val q: String) : FavoritesIntent()
    object ToggleManaging : FavoritesIntent()
    data class ToggleSelect(val id: Long) : FavoritesIntent()
    object SelectAll : FavoritesIntent()
    object ClearSelection : FavoritesIntent()
    object UnfavoriteSelected : FavoritesIntent()
    object DeleteSelected : FavoritesIntent()
    data class SortChanged(val sort: String) : FavoritesIntent()
}
