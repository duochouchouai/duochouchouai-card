package com.example.androidprogram.feature.qr

import android.graphics.Bitmap

data class QrState(
    val generating: Boolean = false,
    val qrBitmap: Bitmap? = null,
    val scanning: Boolean = false,
    val lastResult: String? = null
)

