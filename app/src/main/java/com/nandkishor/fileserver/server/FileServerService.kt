package com.nandkishor.fileserver.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class FileServerService : Service() {

    private var server: FileServer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serverPath = intent?.getStringExtra("SERVER_PATH") ?: return START_NOT_STICKY
        val port = intent.getIntExtra("PORT", 3825)
        val rootPath = intent.getStringExtra("ROOT_PATH") ?: return START_NOT_STICKY
        val rootDir = File(rootPath)

        startForeground(1, createNotification("File Server running on $serverPath"))
        server = FileServer(rootDir, port).apply { start() }

        return START_STICKY
    }


    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(content: String): Notification {
        val channelId = "file_server_channel"
        val channelName = "File Server"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("File Server")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(true)
            .build()
    }
}
