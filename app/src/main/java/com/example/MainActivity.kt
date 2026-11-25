package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.EsmFamilApp
import com.example.viewmodel.EsmFamilViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EsmFamilViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsmFamilApp(viewModel = viewModel)
        }
    }
}
