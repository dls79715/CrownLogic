package com.example.crownlogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crownlogic.ui.GameScreen
import com.example.crownlogic.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppTheme {
        val vm: GameViewModel = viewModel(factory = GameViewModel.factory(applicationContext))
        GameScreen(vm)
      }
    }
  }
}
