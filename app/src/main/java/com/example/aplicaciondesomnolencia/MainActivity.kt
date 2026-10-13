package com.example.aplicaciondesomnolencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aplicaciondesomnolencia.ui.MainScreen
import com.example.aplicaciondesomnolencia.ui.theme.AplicacionDeSomnolenciaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionDeSomnolenciaTheme {
                MainScreen()
            }
        }
    }
}
