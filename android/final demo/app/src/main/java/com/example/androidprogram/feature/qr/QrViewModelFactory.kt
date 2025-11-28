package com.example.androidprogram.feature.qr

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidprogram.repository.CardRepository

class QrViewModelFactory(private val repo: CardRepository, private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QrViewModel(repo, appContext) as T
    }
}

