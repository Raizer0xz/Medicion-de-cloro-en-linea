package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasViewModel
import com.example.medicindecloroenlnea.utils.obtenerWindowSizeClass

@Composable
fun PuntosScreen(
    usuario: UsuarioUiState,
    onIrAlHistorial: (Int) -> Unit,
    onGestionarPuntos: () -> Unit = {},
    viewModel: GranjasViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = obtenerWindowSizeClass()

    LaunchedEffect(usuario.id, usuario.granjasIds) {
        viewModel.configurarAcceso(usuario.granjasIds, usuario.id)
    }

    Column(Modifier.fillMaxSize()) {
        if (state.pendientesSincronizacion > 0) {
            Text(
                "${state.pendientesSincronizacion} mediciones guardadas localmente pendientes de sincronización.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        state.error?.let {
            Text(
                it,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
        if (usuario.rol == RolUsuario.SUPERVISOR || usuario.rol == RolUsuario.JEFATURA) {
            Button(
                onClick = onGestionarPuntos,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Administrar puntos")
            }
        }
        Box(Modifier.weight(1f)) {
            val onMeasure: (Int, Double) -> Unit = { pointId, value ->
                viewModel.registrarMedicion(pointId, value) {}
            }
            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact ->
                    PuntosScreenCompacta(
                        state,
                        viewModel::seleccionarGranja,
                        onIrAlHistorial,
                        onMeasure
                    )

                WindowWidthSizeClass.Medium ->
                    PuntosScreenMediana(
                        state,
                        viewModel::seleccionarGranja,
                        onIrAlHistorial,
                        onMeasure
                    )

                else ->
                    PuntosScreenExpandida(
                        state,
                        viewModel::seleccionarGranja,
                        onIrAlHistorial,
                        onMeasure
                    )
            }
        }
    }
}