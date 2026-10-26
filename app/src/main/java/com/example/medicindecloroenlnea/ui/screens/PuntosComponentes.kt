package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.domain.model.formatearNivel

// Devuelve el color del semáforo según el estado
fun colorDeEstado(estado: EstadoCloro): Color = when (estado) {
    EstadoCloro.NORMAL -> Color(0xFF2E7D32)       // verde
    EstadoCloro.ADVERTENCIA -> Color(0xFFF9A825)  // amarillo
    EstadoCloro.CRITICO -> Color(0xFFC62828)      // rojo
}

// Una tarjeta con un punto de medición
@Composable
fun PuntoItem(
    punto: PuntoMedicion,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onRegistrarMedicion: ((Double) -> Unit)? = null
) {
    var mostrarFormulario by remember(punto.id) { mutableStateOf(false) }
    var valorIngresado by remember(punto.id) { mutableStateOf("") }
    var error by remember(punto.id) { mutableStateOf<String?>(null) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(colorDeEstado(punto.estado), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(punto.codigo + " · " + punto.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${punto.galpon} · ${punto.tipo} · Actualizado: ${punto.ultimaActualizacion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text("${formatearNivel(punto.nivelActual)} mg/L", style = MaterialTheme.typography.titleMedium)
            }
            if (onClick != null || onRegistrarMedicion != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onClick != null) {
                        TextButton(onClick = onClick) { Text("Historial") }
                    }
                    if (onRegistrarMedicion != null) {
                        TextButton(onClick = { mostrarFormulario = true }) { Text("Nueva medición") }
                    }
                }
            }
        }
    }

    if (mostrarFormulario && onRegistrarMedicion != null) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            title = { Text("Medición · ${punto.codigo}") },
            text = {
                OutlinedTextField(
                    value = valorIngresado,
                    onValueChange = { valorIngresado = it.replace(',', '.'); error = null },
                    label = { Text("Cloro libre (mg/L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val value = valorIngresado.toDoubleOrNull()
                    if (value == null || !value.isFinite() || value !in 0.0..20.0) {
                        error = "Ingresa un valor entre 0 y 20 mg/L."
                    } else {
                        onRegistrarMedicion(value)
                        mostrarFormulario = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarFormulario = false }) { Text("Cancelar") } }
        )
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