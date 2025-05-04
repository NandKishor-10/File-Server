package com.nandkishor.fileserver.server

import android.os.Build
import androidx.activity.ComponentActivity
import android.Manifest.permission
import androidx.core.app.ActivityCompat

fun requestStoragePermissions(activity: ComponentActivity) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            permission.READ_MEDIA_IMAGES,
            permission.READ_MEDIA_VIDEO,
            permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(permission.READ_EXTERNAL_STORAGE)
    }

    ActivityCompat.requestPermissions(activity, permissions, 100)
}
