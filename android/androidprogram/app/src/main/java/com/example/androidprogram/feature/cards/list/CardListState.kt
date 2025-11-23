package com.example.androidprogram.feature.cards.list

import com.example.androidprogram.model.Card
import androidx.compose.runtime.Immutable

@Immutable
data class CardListState(
    val loading: Boolean = false,
    val query: String = "",
    val cards: List<Card> = emptyList(),
    val sort: String = "time"
)
