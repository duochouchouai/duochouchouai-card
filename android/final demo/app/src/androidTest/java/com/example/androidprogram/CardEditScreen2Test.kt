package com.example.androidprogram

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.androidprogram.feature.cards.edit.CardEditIntent
import com.example.androidprogram.feature.cards.edit.CardEditScreen
import com.example.androidprogram.feature.cards.edit.CardEditViewModel
import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import com.example.androidprogram.repository.CardRepository
import org.junit.Rule
import org.junit.Test

class CardEditScreen2Test {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun saveEnabledWhenRequiredFieldsValid() {
        val dao = object : CardDao {
            override suspend fun insert(card: Card): Long = 1
            override suspend fun update(card: Card) {}
            override suspend fun softDelete(id: Long, deletedAt: Long) {}
            override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override fun searchByNameOrCompany(q: String) = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override suspend fun getById(id: Long): Card? = null
            override fun getFavorites() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
        }
        val vm = CardEditViewModel(CardRepository(dao), null)
        composeTestRule.setContent { CardEditScreen(vm = vm, onSaved = {}) }
        composeTestRule.onNodeWithText("姓名").performTextInput("张三")
        composeTestRule.onNodeWithText("头像URI").performTextInput("android.resource://com.example.androidprogram/drawable/ic_launcher_foreground")
        composeTestRule.onNodeWithText("职位").performTextInput("工程师")
        composeTestRule.onNodeWithText("保存").assertIsEnabled()
    }
}

