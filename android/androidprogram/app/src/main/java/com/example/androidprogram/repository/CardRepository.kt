package com.example.androidprogram.repository

import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CardRepository(private val dao: CardDao) {
    fun getAll(): Flow<List<Card>> = dao.getAll()

    fun getFavorites(): Flow<List<Card>> = dao.getFavorites()

    fun search(query: String): Flow<List<Card>> = dao.searchByNameOrCompany("%$query%")

    suspend fun create(card: Card): Long = withContext(Dispatchers.IO) {
        dao.insert(card)
    }

    suspend fun update(card: Card) = withContext(Dispatchers.IO) {
        dao.update(card)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.softDelete(id, System.currentTimeMillis())
    }

    suspend fun getById(id: Long): Card? = withContext(Dispatchers.IO) {
        dao.getById(id)
    }
}

