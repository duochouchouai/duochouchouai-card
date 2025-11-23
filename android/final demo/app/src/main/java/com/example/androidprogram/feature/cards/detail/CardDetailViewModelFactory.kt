package com.example.androidprogram.feature.cards.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidprogram.repository.CardRepository

class CardDetailViewModelFactory(private val repo: CardRepository, private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardDetailViewModel(repo, appContext) as T
    }
}

