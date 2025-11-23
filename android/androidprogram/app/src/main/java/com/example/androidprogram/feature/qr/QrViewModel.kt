package com.example.androidprogram.feature.qr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.model.Card
import com.example.androidprogram.repository.CardRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QrViewModel(private val repo: CardRepository, private val appContext: Context) : ViewModel() {
    private val _state = MutableStateFlow(QrState())
    val state: StateFlow<QrState> = _state

    fun dispatch(intent: QrIntent) {
        when (intent) {
            is QrIntent.GenerateForCard -> generate(intent.id)
            is QrIntent.StartScan -> _state.update { it.copy(scanning = intent.enabled) }
            QrIntent.SaveGenerated -> save()
        }
    }

    private fun generate(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(generating = true) }
            val c: Card? = repo.getById(id)
            val payload = if (c != null) listOfNotNull(
                c.name,
                c.position,
                c.company,
                c.phone,
                c.email
            ).joinToString("|") else ""
            val matrix: BitMatrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 640, 640)
            val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            _state.update { it.copy(generating = false, qrBitmap = bmp) }
        }
    }

    fun onScanResult(text: String) {
        _state.update { it.copy(lastResult = text) }
        val parts = text.split("|")
        val name = parts.getOrNull(0) ?: return
        val position = parts.getOrNull(1) ?: ""
        val company = parts.getOrNull(2)
        val phone = parts.getOrNull(3)
        val email = parts.getOrNull(4)
        viewModelScope.launch {
            repo.create(
                Card(
                    name = name,
                    avatarUri = "android.resource://com.example.androidprogram/drawable/ic_launcher_foreground",
                    position = position,
                    company = company,
                    phone = phone,
                    email = email,
                    favorite = true
                )
            )
        }
    }

    private fun save() {
        val bmp = _state.value.qrBitmap ?: return
        viewModelScope.launch {
            val resolver = appContext.contentResolver
            val name = "card_qr_${System.currentTimeMillis()}"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@launch
            resolver.openOutputStream(uri)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val v = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                resolver.update(uri, v, null, null)
            }
        }
    }
}
