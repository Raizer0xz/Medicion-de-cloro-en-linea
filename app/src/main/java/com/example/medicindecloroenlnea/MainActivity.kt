package com.example.medicindecloroenlnea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.navigation.AppNavigation
import com.example.medicindecloroenlnea.ui.theme.MediciónDeCloroEnLíneaTheme
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediciónDeCloroEnLíneaTheme {
                val usuarioViewModel: UsuarioViewModel = viewModel()
                val mainViewModel: MainViewModel = viewModel()
                AppNavigation(
                    usuarioViewModel = usuarioViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}