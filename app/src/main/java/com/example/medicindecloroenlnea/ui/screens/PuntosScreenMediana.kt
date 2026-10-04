package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.theme.MediciónDeCloroEnLíneaTheme
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasUiState

@Composable
fun PuntosScreenMediana(state: GranjasUiState, onGranjaClick: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Puntos de medición", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        SelectorGranjas(state.granjas, state.granjaSeleccionada, onGranjaClick)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.puntos) { PuntoItem(it) }
        }
    }
}

@Preview(name = "Mediana", widthDp = 700, heightDp = 900, showBackground = true)
@Composable
fun PreviewMediana() {
    MediciónDeCloroEnLíneaTheme { PuntosScreenMediana(estadoEjemplo) {} }
}