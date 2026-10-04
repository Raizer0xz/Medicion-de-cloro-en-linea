package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasViewModel
import com.example.medicindecloroenlnea.utils.obtenerWindowSizeClass

@Composable
fun PuntosScreen(viewModel: GranjasViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = obtenerWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact ->
            PuntosScreenCompacta(state, viewModel::seleccionarGranja)
        WindowWidthSizeClass.Medium ->
            PuntosScreenMediana(state, viewModel::seleccionarGranja)
        else ->
            PuntosScreenExpandida(state, viewModel::seleccionarGranja)
    }
}