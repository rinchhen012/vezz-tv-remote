package com.rinchhen.vezzremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinchhen.vezzremote.ui.MainViewModel
import com.rinchhen.vezzremote.ui.VezzRemoteRoot
import com.rinchhen.vezzremote.ui.theme.VezzRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VezzRemoteTheme {
                val viewModel: MainViewModel = viewModel()
                VezzRemoteRoot(viewModel)
            }
        }
    }
}
