package com.example.androidprogram.feature.cards.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun CardDetailScreen(vm: CardDetailViewModel, id: Long, onGenerateQr: (Long) -> Unit) {
    val s by vm.state.collectAsState()
    LaunchedEffect(id) { vm.dispatch(CardDetailIntent.Load(id)) }
    Scaffold { paddingValues ->
        if (s.loading) {
            CircularProgressIndicator()
        } else {
            val c = s.card
            if (c != null) {
                val ctx = LocalContext.current
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    val req = ImageRequest.Builder(ctx).data(c.avatarUri).placeholder(com.example.androidprogram.R.drawable.ic_launcher_foreground).error(com.example.androidprogram.R.drawable.ic_launcher_foreground).build()
                    Image(painter = rememberAsyncImagePainter(model = req), contentDescription = null, contentScale = ContentScale.Crop)
                    Text(c.name)
                    Text(c.position)
                    if (!c.company.isNullOrBlank()) Text(c.company ?: "")
                    if (!c.phone.isNullOrBlank()) Text(c.phone ?: "")
                    if (!c.email.isNullOrBlank()) Text(c.email ?: "")
                    if (!c.address.isNullOrBlank()) Text(c.address ?: "")
                    if (!c.note.isNullOrBlank()) Text(c.note ?: "")
                    Button(onClick = { vm.dispatch(CardDetailIntent.Share) }) { Text("分享") }
                    Button(onClick = { onGenerateQr(c.id) }) { Text("生成二维码") }
                }
            }
        }
    }
}
