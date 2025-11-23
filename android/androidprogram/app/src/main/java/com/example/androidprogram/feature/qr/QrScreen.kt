package com.example.androidprogram.feature.qr

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScreen(vm: QrViewModel) {
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current
    Scaffold { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("二维码")
            val hasMediaPerm = remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) }
            val mediaLauncher = androidx.activity.compose.rememberLauncherForActivityResult(contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
                hasMediaPerm.value = granted
                if (granted) vm.dispatch(QrIntent.SaveGenerated)
            }
            Button(onClick = {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    if (hasMediaPerm.value) vm.dispatch(QrIntent.SaveGenerated) else mediaLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else vm.dispatch(QrIntent.SaveGenerated)
            }, enabled = s.qrBitmap != null) { Text("保存到相册") }
            if (s.qrBitmap != null) {
                Image(painter = rememberAsyncImagePainter(s.qrBitmap), contentDescription = null)
            }
            val hasCamPerm = remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
                hasCamPerm.value = granted
                if (granted) vm.dispatch(QrIntent.StartScan(true))
            }
            Button(onClick = {
                if (hasCamPerm.value) vm.dispatch(QrIntent.StartScan(true)) else launcher.launch(Manifest.permission.CAMERA)
            }) { Text("开始扫描") }
            if (s.scanning) {
                Button(onClick = { vm.dispatch(QrIntent.StartScan(false)) }) { Text("停止扫描") }
            }
            if (s.scanning && hasCamPerm.value) {
                CameraPreview(onResult = { vm.onScanResult(it) })
            }
            if (s.generating) {
                CircularProgressIndicator()
            }
            if (!s.lastResult.isNullOrBlank()) {
                Text("识别结果：${s.lastResult}")
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreview(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = rememberCameraProvider()
    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder().build().apply {
            setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    val options = BarcodeScannerOptions.Builder().build()
                    val scanner = BarcodeScanning.getClient(options)
                    scanner.process(image).addOnSuccessListener { barcodes ->
                        val first = barcodes.firstOrNull()?.rawValue
                        if (!first.isNullOrBlank()) onResult(first)
                    }.addOnCompleteListener { imageProxy.close() }.addOnFailureListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }
        previewView
    })
}

@Composable
private fun rememberCameraProvider(): java.util.concurrent.Future<ProcessCameraProvider> {
    val context = LocalContext.current
    val provider = ProcessCameraProvider.getInstance(context)
    LaunchedEffect(Unit) { }
    return provider
}
