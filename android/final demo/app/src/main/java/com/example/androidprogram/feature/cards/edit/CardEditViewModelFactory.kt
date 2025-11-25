package com.example.androidprogram.feature.cards.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidprogram.repository.CardRepository

class CardEditViewModelFactory(private val repo: CardRepository, private val id: Long? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardEditViewModel(repo, id) as T
    }
}

