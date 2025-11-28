package com.example.androidprogram.feature.cards.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidprogram.repository.CardRepository

class CardListViewModelFactory(private val repo: CardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardListViewModel(repo) as T
    }
}

