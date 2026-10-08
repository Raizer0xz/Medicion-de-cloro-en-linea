package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion

// Devuelve el color del semáforo según el estado
fun colorDeEstado(estado: EstadoCloro): Color = when (estado) {
    EstadoCloro.NORMAL -> Color(0xFF2E7D32)       // verde
    EstadoCloro.ADVERTENCIA -> Color(0xFFF9A825)  // amarillo
    EstadoCloro.CRITICO -> Color(0xFFC62828)      // rojo
}

// Una tarjeta con un punto de medición
@Composable
fun PuntoItem(punto: PuntoMedicion, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .background(colorDeEstado(punto.estado), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(punto.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Actualizado: ${punto.ultimaActualizacion}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("${punto.nivelActual} mg/L", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Fila de botones para elegir la granja
@Composable
fun SelectorGranjas(
    granjas: List<Granja>,
    seleccionada: Int?,
    onGranjaClick: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(granjas) { g ->
            FilterChip(
                selected = g.id == seleccionada,
                onClick = { onGranjaClick(g.id) },
                label = { Text(g.nombre) }
            )
        }
    }
}