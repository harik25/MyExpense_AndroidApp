package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppThemeMode
import com.example.ui.BachatViewModel
import com.example.ui.navigation.BachatMainContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  private var quickAddTrigger by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIntentAction(intent)

    setContent {
      val viewModel: BachatViewModel = viewModel()
      val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

      val darkTheme = when (appSettings.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
      }

      SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
      }

      MyApplicationTheme(themeMode = appSettings.themeMode) {
        BachatMainContainer(
          viewModel = viewModel,
          quickAddTrigger = quickAddTrigger,
          onQuickAddHandled = { quickAddTrigger = false }
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntentAction(intent)
  }

  private fun handleIntentAction(intent: Intent?) {
    val quickAction = intent?.getStringExtra("quick_action")
    if (quickAction == "new_transaction") {
      quickAddTrigger = true
    }
  }
}

