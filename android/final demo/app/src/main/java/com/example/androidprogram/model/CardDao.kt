package com.example.androidprogram.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)

    @Query("UPDATE cards SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long)

    @Query("SELECT * FROM cards WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Card>>

    @Query(
        "SELECT * FROM cards WHERE isDeleted = 0 AND (name LIKE :q OR company LIKE :q OR position LIKE :q OR department LIKE :q OR email LIKE :q OR phone LIKE :q OR address LIKE :q OR note LIKE :q OR category LIKE :q) ORDER BY name ASC"
    )
    fun searchByNameOrCompany(q: String): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): Card?

    @Query("SELECT * FROM cards WHERE isDeleted = 0 AND favorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<Card>>
}
