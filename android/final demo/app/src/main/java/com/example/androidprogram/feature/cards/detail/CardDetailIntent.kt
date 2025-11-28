package com.example.androidprogram.feature.cards.detail

sealed class CardDetailIntent {
    data class Load(val id: Long) : CardDetailIntent()
    object Share : CardDetailIntent()
}

