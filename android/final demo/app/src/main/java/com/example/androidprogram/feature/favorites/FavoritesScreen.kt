package com.example.androidprogram.feature.favorites

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(vm: FavoritesViewModel, onBack: () -> Unit, onOpen: (Long) -> Unit, onEdit: (Long) -> Unit, onAdd: () -> Unit, onOpenQR: () -> Unit) {
    val s by vm.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "收藏",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") } },
                actions = {
                    Text(
                        text = if (s.managing) "退出批量" else "批量管理",
                        modifier = Modifier.padding(horizontal = 12.dp).clickable { vm.dispatch(FavoritesIntent.ToggleManaging) },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            com.example.androidprogram.ui.components.ExpandableFloatingActionButton(
                mainIcon = Icons.Filled.Add,
                mainContentDescription = "新增名片",
                actions = listOf(
                    com.example.androidprogram.ui.components.FabAction(
                        icon = Icons.Filled.Person,
                        label = "手动添加",
                        onClick = onAdd,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    com.example.androidprogram.ui.components.FabAction(
                        icon = Icons.Filled.QrCode,
                        label = "扫码添加",
                        onClick = onOpenQR,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        },
        bottomBar = {
            if (s.managing) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已选 ${s.selected.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.dispatch(FavoritesIntent.SelectAll) }) { Text("全选") }
                        TextButton(onClick = { vm.dispatch(FavoritesIntent.ClearSelection) }) { Text("清空") }
                        TextButton(onClick = { vm.dispatch(FavoritesIntent.UnfavoriteSelected) }, enabled = s.selected.isNotEmpty()) { Text("取消收藏") }
                        
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            OutlinedTextField(
                value = s.query,
                onValueChange = { vm.dispatch(FavoritesIntent.QueryChanged(it)) },
                label = { Text("搜索姓名、公司或职位") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = s.sort == "time",
                    onClick = { vm.dispatch(FavoritesIntent.SortChanged("time")) },
                    label = { Text("按时间") },
                    leadingIcon = if (s.sort == "time") { { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                FilterChip(
                    selected = s.sort == "name",
                    onClick = { vm.dispatch(FavoritesIntent.SortChanged("name")) },
                    label = { Text("按姓名") },
                    leadingIcon = if (s.sort == "name") { { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                FilterChip(
                    selected = s.sort == "company",
                    onClick = { vm.dispatch(FavoritesIntent.SortChanged("company")) },
                    label = { Text("按公司") },
                    leadingIcon = if (s.sort == "company") { { Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                FilterChip(
                    selected = s.sort == "category",
                    onClick = { vm.dispatch(FavoritesIntent.SortChanged("category")) },
                    label = { Text("按分类") },
                    leadingIcon = if (s.sort == "category") { { Icon(Icons.Filled.Work, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(s.cards, key = { it.id }) { c ->
                    val gradientColors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { if (s.managing) vm.dispatch(FavoritesIntent.ToggleSelect(c.id)) else onOpen(c.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(gradientColors),
                                    alpha = 0.1f
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val ctx = LocalContext.current
                                val req = ImageRequest.Builder(ctx)
                                    .data(c.avatarUri)
                                    .placeholder(com.example.androidprogram.R.drawable.ic_launcher_foreground)
                                    .error(com.example.androidprogram.R.drawable.ic_launcher_foreground)
                                    .crossfade(true)
                                    .build()
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = req),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = c.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    if (!c.company.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                            Text(text = c.company!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                    if (!c.department.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Filled.Work, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            Text(text = c.department!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                    if (c.position.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            Text(text = c.position, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                    if (!c.category.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .background(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = c.category!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        }
                                    }
                                }
                                if (s.managing) {
                                    Checkbox(
                                        checked = s.selected.contains(c.id),
                                        onCheckedChange = { vm.dispatch(FavoritesIntent.ToggleSelect(c.id)) }
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { vm.dispatch(FavoritesIntent.ToggleFavorite(c.id, !c.favorite)) },
                                        modifier = Modifier
                                            .background(color = if (c.favorite) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent, shape = CircleShape)
                                    ) {
                                        Icon(imageVector = if (c.favorite) Icons.Filled.Star else Icons.Outlined.StarOutline, contentDescription = null, tint = if (c.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                    }
                                    IconButton(
                                        onClick = { onEdit(c.id) },
                                        modifier = Modifier
                                            .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    IconButton(
                                        onClick = { vm.dispatch(FavoritesIntent.DeleteSingle(c.id)) },
                                        modifier = Modifier
                                            .background(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
