package com.nandkishor.fileserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.nandkishor.fileserver.server.MyApp
import com.nandkishor.fileserver.ui.theme.FileServerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileServerTheme {
                Surface {
                    MyApp(this)
                }
            }
        }
    }
}
