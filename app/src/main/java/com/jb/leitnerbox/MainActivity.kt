package com.jb.leitnerbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jb.leitnerbox.ui.MainScreen
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeitnerBoxTheme {
                MainScreen()
            }
        }
    }
}