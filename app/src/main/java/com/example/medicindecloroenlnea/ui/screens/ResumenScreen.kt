package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.ui.viewmodels.ResumenViewModel

@Composable
fun ResumenScreen(viewModel: ResumenViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Resumen general", style = MaterialTheme.typography.headlineSmall)
        TarjetaResumen("Puntos totales", state.total, MaterialTheme.colorScheme.primary)
        TarjetaResumen("Normales", state.normales, colorDeEstado(EstadoCloro.NORMAL))
        TarjetaResumen("Advertencias", state.advertencias, colorDeEstado(EstadoCloro.ADVERTENCIA))
        TarjetaResumen("Críticos", state.criticos, colorDeEstado(EstadoCloro.CRITICO))
    }
}

@Composable
private fun TarjetaResumen(titulo: String, cantidad: Int, color: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(14.dp).background(color, CircleShape))
            Spacer(Modifier.width(12.dp))
            Text(titulo, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("$cantidad", style = MaterialTheme.typography.headlineSmall)
        }
    }
}