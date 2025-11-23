package com.example.androidprogram.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUri: String,
    val position: String,
    val department: String? = null,
    val company: String? = null,
    val category: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val note: String? = null,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
