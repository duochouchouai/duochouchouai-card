package com.example.androidprogram.feature.cards.edit

data class CardEditState(
    val name: String = "",
    val avatarUri: String = "",
    val position: String = "",
    val department: String = "",
    val company: String = "",
    val category: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val note: String = "",
    val saving: Boolean = false,
    val canSave: Boolean = false
)
