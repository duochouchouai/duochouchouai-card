package com.example.androidprogram.feature.cards.detail

import com.example.androidprogram.model.Card

data class CardDetailState(
    val loading: Boolean = false,
    val card: Card? = null
)

