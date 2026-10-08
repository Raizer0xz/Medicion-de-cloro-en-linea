package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.theme.MediciónDeCloroEnLíneaTheme
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasUiState

@Composable
fun PuntosScreenExpandida(state: GranjasUiState, onGranjaClick: (Int) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        // Panel izquierdo: lista de granjas
        Column(Modifier.width(280.dp).fillMaxHeight().padding(16.dp)) {
            Text("Granjas", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(state.granjas) { g ->
                    NavigationDrawerItem(
                        label = { Text(g.nombre) },
                        selected = g.id == state.granjaSeleccionada,
                        onClick = { onGranjaClick(g.id) }
                    )
                }
            }
        }
        // Panel derecho: grilla de puntos
        Column(Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            Text("Puntos de medición", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.puntos) { PuntoItem(it) }
            }
        }
    }
}

@Preview(name = "Expandida", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
fun PreviewExpandida() {
    MediciónDeCloroEnLíneaTheme { PuntosScreenExpandida(estadoEjemplo) {} }
}