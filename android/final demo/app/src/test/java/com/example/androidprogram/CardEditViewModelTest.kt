package com.example.androidprogram

import com.example.androidprogram.feature.cards.edit.CardEditIntent
import com.example.androidprogram.feature.cards.edit.CardEditViewModel
import com.example.androidprogram.model.Card
import com.example.androidprogram.model.CardDao
import com.example.androidprogram.repository.CardRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardEditViewModelTest {
    @Test
    fun validationWorks() {
        val fakeDao = object : CardDao {
            override suspend fun insert(card: Card): Long = 1
            override suspend fun update(card: Card) {}
            override suspend fun softDelete(id: Long, deletedAt: Long) {}
            override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override fun searchByNameOrCompany(q: String) = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
            override suspend fun getById(id: Long): Card? = null
            override fun getFavorites() = kotlinx.coroutines.flow.flowOf(emptyList<Card>())
        }
        val repo = CardRepository(fakeDao)
        val vm = CardEditViewModel(repo, null)
        vm.dispatch(CardEditIntent.NameChanged("")) { }
        vm.dispatch(CardEditIntent.AvatarChanged("")) { }
        vm.dispatch(CardEditIntent.PositionChanged("工程师")) { }
        assertFalse(vm.state.value.canSave)

        vm.dispatch(CardEditIntent.NameChanged("张三")) { }
        vm.dispatch(CardEditIntent.AvatarChanged("file://avatar.png")) { }
        vm.dispatch(CardEditIntent.PhoneChanged("13300000000")) { }
        vm.dispatch(CardEditIntent.EmailChanged("abc@x.com")) { }
        assertTrue(vm.state.value.canSave)
    }
}
