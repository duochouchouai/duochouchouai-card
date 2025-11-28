package com.example.androidprogram

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.androidprogram.feature.cards.list.CardListScreen
import com.example.androidprogram.feature.cards.list.CardListViewModel
import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import com.example.androidprogram.repository.CardRepository
import org.junit.Rule
import org.junit.Test

class CardListScreen2Test {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun sortChipsVisible() {
        val dao = object : CardDao {
            override suspend fun insert(card: Card): Long = 1
            override suspend fun update(card: Card) {}
            override suspend fun softDelete(id: Long, deletedAt: Long) {}
            override fun getAll() = kotlinx.coroutines.flow.flowOf(listOf(Card(id = 1, name = "张三", avatarUri = "a", position = "工程师")))
            override fun searchByNameOrCompany(q: String) = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override suspend fun getById(id: Long): Card? = null
            override fun getFavorites() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
        }
        val vm = CardListViewModel(CardRepository(dao))
        composeTestRule.setContent {
            CardListScreen(vm = vm, onAdd = {}, onOpen = {}, onOpenQR = {}, onOpenFavorites = {}, onLogout = {})
        }
        composeTestRule.onNodeWithText("按时间").assertIsDisplayed()
        composeTestRule.onNodeWithText("按姓名").assertIsDisplayed()
        composeTestRule.onNodeWithText("按公司").assertIsDisplayed()
        composeTestRule.onNodeWithText("按分类").assertIsDisplayed()
    }
}

