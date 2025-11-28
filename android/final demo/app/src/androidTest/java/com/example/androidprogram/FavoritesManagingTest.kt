package com.example.androidprogram

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidprogram.feature.favorites.FavoritesScreen
import com.example.androidprogram.feature.favorites.FavoritesViewModel
import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import com.example.androidprogram.repository.CardRepository
import org.junit.Rule
import org.junit.Test

class FavoritesManagingTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun managingModeShowsBatchActions() {
        val dao = object : CardDao {
            override suspend fun insert(card: Card): Long = 1
            override suspend fun update(card: Card) {}
            override suspend fun softDelete(id: Long, deletedAt: Long) {}
            override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override fun searchByNameOrCompany(q: String) = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override suspend fun getById(id: Long): Card? = null
            override fun getFavorites() = kotlinx.coroutines.flow.flowOf(listOf(Card(id = 1, name = "张三", avatarUri = "a", position = "工程师", favorite = true)))
        }
        val vm = FavoritesViewModel(CardRepository(dao))
        composeTestRule.setContent { FavoritesScreen(vm = vm, onOpen = {}) }
        composeTestRule.onNodeWithText("批量管理").performClick()
        composeTestRule.onNodeWithText("全选").assertIsDisplayed()
        composeTestRule.onNodeWithText("清空选择").assertIsDisplayed()
        composeTestRule.onNodeWithText("取消收藏所选").assertIsDisplayed()
        composeTestRule.onNodeWithText("删除所选").assertIsDisplayed()
    }
}

