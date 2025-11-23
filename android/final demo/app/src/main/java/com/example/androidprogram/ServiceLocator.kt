package com.example.androidprogram

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.room.Room
import com.example.androidprogram.auth.AuthManager
import com.example.androidprogram.model.AppDatabase
import com.example.androidprogram.repository.CardRepository

object ServiceLocator {
    private var database: AppDatabase? = null
    private var cardRepository: CardRepository? = null
    private var authManager: AuthManager? = null

    fun provideDatabase(context: Context): AppDatabase {
        val current = database
        if (current != null) return current
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cards.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build()
        database = db
        return db
    }

    fun provideCardRepository(context: Context): CardRepository {
        val current = cardRepository
        if (current != null) return current
        val repo = CardRepository(provideDatabase(context).cardDao())
        cardRepository = repo
        return repo
    }

    fun provideAuthManager(context: Context): AuthManager {
        val current = authManager
        if (current != null) return current
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val manager = AuthManager(prefs)
        authManager = manager
        return manager
    }
}
