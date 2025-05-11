package com.nandkishor.fileserver.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun QrCodeView(data: String) {
    val bitmap = remember(data) {
        generateQr(data, size = 512)
    }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Image(
        bitmap = imageBitmap,
        contentDescription = "QR Code",
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer),
        modifier = Modifier
            .fillMaxSize(0.8f)
    )
}
