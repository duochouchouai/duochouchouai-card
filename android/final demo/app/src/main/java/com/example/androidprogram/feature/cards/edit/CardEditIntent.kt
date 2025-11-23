package com.example.androidprogram.feature.cards.edit

sealed class CardEditIntent {
    data class NameChanged(val v: String) : CardEditIntent()
    data class AvatarChanged(val v: String) : CardEditIntent()
    data class PositionChanged(val v: String) : CardEditIntent()
    data class DepartmentChanged(val v: String) : CardEditIntent()
    data class CompanyChanged(val v: String) : CardEditIntent()
    data class CategoryChanged(val v: String) : CardEditIntent()
    data class PhoneChanged(val v: String) : CardEditIntent()
    data class EmailChanged(val v: String) : CardEditIntent()
    data class AddressChanged(val v: String) : CardEditIntent()
    data class NoteChanged(val v: String) : CardEditIntent()
    object Save : CardEditIntent()
    object SaveToList : CardEditIntent()
    object SaveToFavorites : CardEditIntent()
}
