package com.example.androidprogram.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

@Entity(
    tableName = "cards",
    indices = [
        Index(value = ["name"]),
        Index(value = ["company"]),
        Index(value = ["position"]),
        Index(value = ["department"]),
        Index(value = ["email"]),
        Index(value = ["phone"]),
        Index(value = ["address"]),
        Index(value = ["note"]),
        Index(value = ["category"])
    ]
)
@Immutable
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
