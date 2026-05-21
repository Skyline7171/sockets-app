package com.example.sockets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.sockets.ui.theme.SocketsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocketsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val urlDeRender = "wss://socket-server-sistemas-distribuidos.onrender.com/ws"
                    AppNavigationContainer(urlServer = urlDeRender, innerPadding = innerPadding)
                }
            }
        }
    }
}