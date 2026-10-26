package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.Alerta
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.ui.viewmodels.AlertasViewModel

@Composable
fun AlertasScreen(alertasViewModel: AlertasViewModel = viewModel()) {
    val state by alertasViewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.cargando -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
        }

        state.alertas.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No hay alertas activas.")
                Text(
                    "Las alertas se generan cuando una medición está fuera del rango normal.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.alertas, key = Alerta::id) { alerta ->
                    AlertaCard(alerta)
                }
            }
        }
    }
}

@Composable
private fun AlertaCard(alerta: Alerta) {
    val color = colorDeEstado(alerta.estado)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (alerta.estado) {
                    EstadoCloro.NORMAL -> "Normal"
                    EstadoCloro.ADVERTENCIA -> "Advertencia"
                    EstadoCloro.CRITICO -> "Crítico"
                },
                color = color,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Punto de medición: ${alerta.puntoId}")
            Text("Nivel: ${"%.2f".format(alerta.nivelCloro)} mg/L")
            Text("Fecha: ${alerta.fechaHora}")
            Text(if (alerta.atendida) "Atendida" else "Pendiente")
        }
    }
}
