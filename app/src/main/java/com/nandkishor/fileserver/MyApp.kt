package com.nandkishor.fileserver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
fun MyApp(activity: ComponentActivity) {
    val context = LocalContext.current
    val port = 3825
    val ipAddress = remember { getLocalIpAddress(activity) }
    var rootDirState by remember { mutableStateOf<File?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isRunningFlow = ServerPrefs.isServerRunningFlow(context)
    val isRunning by isRunningFlow.collectAsState(initial = false)

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


    if (rootPath.isBlank()){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.padding(24.dp))
            Text("Setting up File System...")
        }
    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📂 File Server", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.padding(vertical = 16.dp))
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
                }
            ) {
                Text(if (isRunning) "Stop Server" else "Start Server")
            }
            Spacer(Modifier.padding(vertical = 16.dp))
            if (isRunning) {
                Text("Server running at:\nhttp://$ipAddress:$port")
                Spacer(Modifier.padding(vertical = 8.dp))
                Text("Serving files from:\n${rootPath}")
            } else {
                Text("Server is stopped")
            }
        }
    }
}
