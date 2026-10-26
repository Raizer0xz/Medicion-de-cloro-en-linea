package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.ui.theme.MediciónDeCloroEnLíneaTheme
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasUiState

@Composable
fun PuntosScreenCompacta(state: GranjasUiState, onGranjaClick: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Puntos de medición", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        SelectorGranjas(state.granjas, state.granjaSeleccionada, onGranjaClick)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.puntos) { PuntoItem(it) }
        }
    }
}

@Preview(name = "Compacta", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
fun PreviewCompacta() {
    val estadoEjemplo = GranjasUiState(
        granjas = listOf(Granja(1, "Granja Los Aromos"), Granja(2, "Granja El Roble")),
        puntos = listOf(
            PuntoMedicion(1, 1, "Galpón 1 - Bebederos", 0.9, EstadoCloro.NORMAL, "2026-10-03T08:30"),
            PuntoMedicion(2, 1, "Galpón 2 - Bebederos", 0.4, EstadoCloro.ADVERTENCIA, "2026-10-03T08:30"),
            PuntoMedicion(3, 1, "Estanque principal", 0.2, EstadoCloro.CRITICO, "2026-10-03T08:30")
        ),
        granjaSeleccionada = 1,
        cargando = false
    )
    MediciónDeCloroEnLíneaTheme { PuntosScreenCompacta(estadoEjemplo) {} }
}