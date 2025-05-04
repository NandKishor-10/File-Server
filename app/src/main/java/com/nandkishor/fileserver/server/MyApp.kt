package com.nandkishor.fileserver.server

import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun MyApp(activity: ComponentActivity) {
    val port = 3825
    val ipAddress = remember { getLocalIpAddress(activity) }
    val rootDir = remember {
        File(Environment.getExternalStorageDirectory(), "File Server").apply { mkdirs() }
    }

    var isRunning by remember { mutableStateOf(false) }
    var server by remember { mutableStateOf<FileServer?>(null) }

    LaunchedEffect(Unit) {
        requestStoragePermissions(activity)
    }

    DisposableEffect(Unit) {
        onDispose {
            server?.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📂 File Server", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.padding(vertical = 16.dp))
        Button(onClick = {
            if (!isRunning) {
                server = FileServer(rootDir, port).also { it.start() }
                isRunning = true
            } else {
                server?.stop()
                server = null
                isRunning = false
            }
        }) {
            Text(if (isRunning) "Stop Server" else "Start Server")
        }
        Spacer(Modifier.padding(vertical = 16.dp))
        if (isRunning) {
            Text("Server running at:\nhttp://$ipAddress:$port")
            Spacer(Modifier.padding(vertical = 8.dp))
            Text("Serving files from:\n${rootDir.absolutePath}")
        } else {
            Text("Server is stopped")
        }
    }
}

