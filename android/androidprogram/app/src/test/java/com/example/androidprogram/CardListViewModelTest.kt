package com.example.androidprogram

import com.example.androidprogram.feature.cards.list.CardListIntent
import com.example.androidprogram.feature.cards.list.CardListViewModel
import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import com.example.androidprogram.repository.CardRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Test

class CardListViewModelTest {
    private class FakeDao : CardDao {
        var updatedFavorite: Boolean? = null
        override suspend fun insert(card: Card): Long = 1
        override suspend fun update(card: Card) { updatedFavorite = card.favorite }
        override suspend fun softDelete(id: Long, deletedAt: Long) {}
        override fun getAll() = flowOf(listOf(Card(id = 1, name = "张三", avatarUri = "a", position = "工程师")))
        override fun searchByNameOrCompany(q: String) = flowOf(emptyList<Card>())
        override suspend fun getById(id: Long): Card? = null
        override fun getFavorites() = flowOf(emptyList<Card>())
    }

    @Test
    fun toggleFavoriteUpdatesRepo() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val dao = FakeDao()
        val vm = CardListViewModel(CardRepository(dao))
        vm.dispatch(CardListIntent.ToggleFavorite(id = 1, favorite = true))
        Thread.sleep(50)
        assertTrue(dao.updatedFavorite == true)
        Dispatchers.resetMain()
    }
}
