package com.example.androidprogram.feature.cards.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardDetailViewModel(private val repo: CardRepository, private val appContext: Context) : ViewModel() {
    private val _state = MutableStateFlow(CardDetailState())
    val state: StateFlow<CardDetailState> = _state

    fun dispatch(intent: CardDetailIntent) {
        when (intent) {
            is CardDetailIntent.Load -> load(intent.id)
            CardDetailIntent.Share -> share()
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val c = repo.getById(id)
            _state.update { it.copy(loading = false, card = c) }
        }
    }

    private fun share() {
        val c: Card = _state.value.card ?: return
        val text = listOf(
            c.name,
            c.position,
            c.company ?: "",
            c.phone ?: "",
            c.email ?: ""
        ).filter { it.isNotBlank() }.joinToString(" | ")
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(Intent.createChooser(intent, "分享名片"))
    }
}

