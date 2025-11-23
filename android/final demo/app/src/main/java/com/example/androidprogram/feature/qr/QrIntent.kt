package com.example.androidprogram.feature.qr

sealed class QrIntent {
    data class GenerateForCard(val id: Long) : QrIntent()
    data class StartScan(val enabled: Boolean) : QrIntent()
    object SaveGenerated : QrIntent()
}

