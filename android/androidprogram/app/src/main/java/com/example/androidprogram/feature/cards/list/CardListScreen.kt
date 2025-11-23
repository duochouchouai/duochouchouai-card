package com.example.androidprogram.feature.cards.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.androidprogram.model.Card

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    vm: CardListViewModel,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onOpenQR: () -> Unit,
    onOpenFavorites: () -> Unit,
    onLogout: () -> Unit
) {
    val state by vm.state.collectAsState()
    val pendingDeleteId = remember { mutableStateOf<Long?>(null) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("名片列表") }, actions = {
            Text(text = "二维码", modifier = Modifier.clickable { onOpenQR() }.padding(12.dp))
            Text(text = "收藏", modifier = Modifier.clickable { onOpenFavorites() }.padding(12.dp))
            Text(text = "退出登录", modifier = Modifier.clickable { onLogout() }.padding(12.dp))
        }, colors = TopAppBarDefaults.topAppBarColors())
    }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { vm.dispatch(CardListIntent.QueryChanged(it)) },
                label = { Text("搜索姓名/公司") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            Text(text = "新增名片", modifier = Modifier.padding(start = 16.dp).clickable { onAdd() })
            if (state.loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = state.cards, key = { it.id }) { card ->
                        CardRow(card = card, onOpen = { onOpen(card.id) }, onFavoriteToggle = { fav ->
                            vm.dispatch(CardListIntent.ToggleFavorite(card.id, fav))
                        }, onDelete = { pendingDeleteId.value = card.id })
                    }
                }
            }
        }
    }
    val toDelete = pendingDeleteId.value
    if (toDelete != null) {
        AlertDialog(onDismissRequest = { pendingDeleteId.value = null }, confirmButton = {
            Text(text = "确认", modifier = Modifier.clickable {
                vm.dispatch(CardListIntent.Delete(toDelete))
                pendingDeleteId.value = null
            }.padding(12.dp))
        }, dismissButton = {
            Text(text = "取消", modifier = Modifier.clickable { pendingDeleteId.value = null }.padding(12.dp))
        }, title = { Text("确认删除") }, text = { Text("删除后可在数据中标记软删除") })
    }
}

@Composable
private fun CardRow(card: Card, onOpen: () -> Unit, onFavoriteToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onOpen() }.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val ctx = LocalContext.current
        val req = ImageRequest.Builder(ctx).data(card.avatarUri).placeholder(com.example.androidprogram.R.drawable.ic_launcher_foreground).error(com.example.androidprogram.R.drawable.ic_launcher_foreground).build()
        Image(
            painter = rememberAsyncImagePainter(model = req),
            contentDescription = null,
            modifier = Modifier.clip(MaterialTheme.shapes.small).padding(end = 4.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(card.name)
            Text(card.position)
            if (!card.company.isNullOrBlank()) Text(card.company ?: "")
        }
        Text(text = if (card.favorite) "已收藏" else "收藏", modifier = Modifier.clickable { onFavoriteToggle(!card.favorite) }.padding(8.dp))
        Text(text = "删除", modifier = Modifier.clickable { onDelete() }.padding(8.dp))
    }
}
