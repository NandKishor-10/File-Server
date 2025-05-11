package com.nandkishor.fileserver.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.EncodeHintType

fun generateQr(text: String, size: Int): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 0 // Removes default white border
    )
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bmp = createBitmap(width, height)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT
        }
    }
    return bmp
}
