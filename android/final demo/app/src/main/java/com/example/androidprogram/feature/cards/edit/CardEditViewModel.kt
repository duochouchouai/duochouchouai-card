package com.example.androidprogram.feature.cards.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardEditViewModel(private val repo: CardRepository, private val editingId: Long? = null) : ViewModel() {
    private val _state = MutableStateFlow(CardEditState())
    val state: StateFlow<CardEditState> = _state

    init {
        if (editingId != null) {
            _state.update { it.copy(isEditing = true) }
            viewModelScope.launch {
                val card = repo.getById(editingId)
                if (card != null) {
                    _state.update {
                        it.copy(
                            name = card.name,
                            avatarUri = card.avatarUri,
                            position = card.position,
                            department = card.department ?: "",
                            company = card.company ?: "",
                            category = card.category ?: "",
                            phone = card.phone ?: "",
                            email = card.email ?: "",
                            isEditing = true,
                            canSave = validate(
                                card.name,
                                card.phone ?: "",
                                card.email ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    private fun validate(name: String, phone: String, email: String): Boolean {
        val phoneOk = phone.isBlank() || phone.matches(Regex("^1\\d{10}$"))
        val emailOk = email.isBlank() || email.contains("@")
        return name.isNotBlank() && phoneOk && emailOk
    }

    fun dispatch(intent: CardEditIntent, onSaved: (Long) -> Unit) {
        when (intent) {
            is CardEditIntent.NameChanged -> update { it.copy(name = intent.v) }
            is CardEditIntent.AvatarChanged -> update { it.copy(avatarUri = intent.v) }
            is CardEditIntent.PositionChanged -> update { it.copy(position = intent.v) }
            is CardEditIntent.DepartmentChanged -> update { it.copy(department = intent.v) }
            is CardEditIntent.CompanyChanged -> update { it.copy(company = intent.v) }
            is CardEditIntent.CategoryChanged -> update { it.copy(category = intent.v) }
            is CardEditIntent.PhoneChanged -> update { it.copy(phone = intent.v) }
            is CardEditIntent.EmailChanged -> update { it.copy(email = intent.v) }
            is CardEditIntent.AddressChanged -> update { it.copy(address = intent.v) }
            is CardEditIntent.NoteChanged -> update { it.copy(note = intent.v) }
            CardEditIntent.Save -> save(false, onSaved)
            CardEditIntent.SaveToList -> save(false, onSaved)
            CardEditIntent.SaveToFavorites -> save(true, onSaved)
        }
    }

    private fun update(block: (CardEditState) -> CardEditState) {
        _state.update { s ->
            val ns = block(s)
            ns.copy(canSave = validate(ns.name, ns.phone, ns.email))
        }
    }

    private fun save(toFavorites: Boolean, onSaved: (Long) -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val id = if (editingId == null) {
                repo.create(
                    Card(
                        name = s.name,
                        avatarUri = s.avatarUri,
                        position = s.position,
                        department = s.department.ifBlank { null },
                        company = s.company.ifBlank { null },
                        category = s.category.ifBlank { null },
                        phone = s.phone.ifBlank { null },
                        email = s.email.ifBlank { null },
                        address = s.address.ifBlank { null },
                        note = s.note.ifBlank { null },
                        favorite = toFavorites
                    )
                )
            } else {
                val existing = repo.getById(editingId)
                if (existing != null) {
                    repo.update(
                        existing.copy(
                            name = s.name,
                            avatarUri = s.avatarUri,
                            position = s.position,
                            department = s.department.ifBlank { null },
                            company = s.company.ifBlank { null },
                            category = s.category.ifBlank { null },
                            phone = s.phone.ifBlank { null },
                            email = s.email.ifBlank { null },
                            address = s.address.ifBlank { null },
                            note = s.note.ifBlank { null }
                        )
                    )
                    existing.id
                } else 0L
            }
            _state.update { it.copy(saving = false) }
            if (id > 0) onSaved(id)
        }
    }
}
