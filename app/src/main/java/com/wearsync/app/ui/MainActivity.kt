package com.wearsync.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wearsync.app.ui.theme.WearSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearSyncTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(applicationContext)
                )
                MainScreen(viewModel)
            }
        }
    }
}
