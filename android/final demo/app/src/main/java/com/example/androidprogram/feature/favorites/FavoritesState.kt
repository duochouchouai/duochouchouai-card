package com.example.androidprogram.feature.favorites

import com.example.androidprogram.model.Card

data class FavoritesState(
    val query: String = "",
    val cards: List<Card> = emptyList(),
    val managing: Boolean = false,
    val selected: Set<Long> = emptySet(),
    val sort: String = "time"
)
