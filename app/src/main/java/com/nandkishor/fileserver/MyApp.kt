package com.nandkishor.fileserver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nandkishor.fileserver.qr.QrCodeView
import com.nandkishor.fileserver.server.FileServerService
import com.nandkishor.fileserver.server.ServerPrefs
import com.nandkishor.fileserver.server.getLocalIpAddress
import com.nandkishor.fileserver.server.hasStoragePermissions
import com.nandkishor.fileserver.server.requestStoragePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MyApp(activity: ComponentActivity, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val port = 3825
    val ipAddress = remember { getLocalIpAddress(activity) }
    var rootDirState by remember { mutableStateOf<File?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isRunningFlow = ServerPrefs.isServerRunningFlow(context)
    val isRunning by isRunningFlow.collectAsState(initial = false)
    val clipboardManager = LocalClipboardManager.current
    val serverPath = "http://$ipAddress:$port"
    val screenWidth = LocalConfiguration.current.smallestScreenWidthDp

    LaunchedEffect(Unit) {
        if (!hasStoragePermissions(context)) {
            requestStoragePermissions(activity)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    102
                )
            }
        }
        while (!hasStoragePermissions(context)) {
            delay(200)
        }
        withContext(Dispatchers.IO) {
            val dir = File(Environment.getExternalStorageDirectory(), "File Server")
            if (!dir.exists()) dir.mkdirs()
            rootDirState = dir
        }
    }
    val rootPath = rootDirState?.absolutePath ?: ""


    if (rootPath.isBlank()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.padding(24.dp))
            Text("Setting up File System...")
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            if (isRunning) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .size((0.9 * screenWidth).dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        "Serving path:\n$rootPath",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    )
                    QrCodeView(serverPath)
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(serverPath))
                            Toast.makeText(
                                context,
                                "Server path copied: \n$serverPath",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "$serverPath   ",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            painterResource(R.drawable.copy),
                            "copy",
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .size((0.9 * screenWidth).dp)
                ) {
                    Text(" Server Not Running!!!", style = MaterialTheme.typography.titleLarge)
                }
            }
            Button(
                onClick = {
                    val serviceIntent = Intent(context, FileServerService::class.java).apply {
                        putExtra("SERVER_PATH", "$ipAddress:$port")
                        putExtra("PORT", port)
                        putExtra("ROOT_PATH", rootPath)
                    }
                    if (!isRunning) {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        context.stopService(serviceIntent)
                    }
                    coroutineScope.launch {
                        ServerPrefs.setServerRunning(context, !isRunning)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 128.dp)
            ) {
                Text(if (!isRunning) "Start Server" else " Stop Server")
            }
        }
    }
}