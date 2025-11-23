package com.example.androidprogram.feature.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesScreen(vm: FavoritesViewModel, onOpen: (Long) -> Unit) {
    val s by vm.state.collectAsState()
    Scaffold { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(value = s.query, onValueChange = { vm.dispatch(FavoritesIntent.QueryChanged(it)) }, label = { Text("搜索收藏") }, modifier = Modifier.fillMaxWidth().padding(16.dp))
            Text(text = if (s.managing) "退出批量管理" else "批量管理", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.ToggleManaging) })
            Text(text = "按时间排序", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.SortChanged("time")) })
            Text(text = "按分类排序", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.SortChanged("category")) })
            Text(text = "按姓名排序", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.SortChanged("name")) })
            if (s.managing) {
                Text(text = "全选", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.SelectAll) })
                Text(text = "清空选择", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.ClearSelection) })
                Text(text = "取消收藏所选", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.UnfavoriteSelected) })
                Text(text = "删除所选", modifier = Modifier.padding(horizontal = 16.dp).clickable { vm.dispatch(FavoritesIntent.DeleteSelected) })
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(s.cards) { c ->
                    if (s.managing) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Checkbox(checked = s.selected.contains(c.id), onCheckedChange = { vm.dispatch(FavoritesIntent.ToggleSelect(c.id)) })
                            Text(text = "${c.name} - ${c.company ?: ""}", modifier = Modifier.fillMaxWidth().clickable { onOpen(c.id) })
                            if (!c.category.isNullOrBlank()) Text(text = "分类：${c.category}")
                        }
                    } else {
                        Text(text = "${c.name} - ${c.company ?: ""}", modifier = Modifier.fillMaxWidth().clickable { onOpen(c.id) }.padding(16.dp))
                    }
                }
            }
        }
    }
}
