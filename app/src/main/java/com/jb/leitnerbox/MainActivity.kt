package com.jb.leitnerbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.usecase.settings.GetThemeUseCase
import com.jb.leitnerbox.ui.MainScreen
import com.jb.leitnerbox.core.ui.theme.LeitnerBoxTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var getTheme: GetThemeUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by getTheme.invoke().collectAsStateWithLifecycle(
                initialValue = AppTheme.SYSTEM
            )
            val darkTheme = when (theme) {
                AppTheme.LIGHT  -> false
                AppTheme.DARK   -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            LeitnerBoxTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}
