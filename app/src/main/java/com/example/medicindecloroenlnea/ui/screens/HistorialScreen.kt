package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.DireccionDesviacion
import com.example.medicindecloroenlnea.domain.model.Medicion
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.formatearNivel
import com.example.medicindecloroenlnea.ui.viewmodels.HistorialViewModel

@Composable
fun HistorialScreen(
    puntoId: Int,
    usuario: UsuarioUiState,
    onVolver: () -> Unit,
    historialViewModel: HistorialViewModel = viewModel()
) {
    val state by historialViewModel.uiState.collectAsStateWithLifecycle()
    var mostrarFormulario by rememberSaveable(puntoId) { mutableStateOf(false) }
    var mensaje by rememberSaveable(puntoId) { mutableStateOf<String?>(null) }

    LaunchedEffect(puntoId, usuario.id, usuario.granjasIds) {
        historialViewModel.configurar(puntoId, usuario.id, usuario.granjasIds)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Historial · Punto $puntoId", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onVolver) { Text("Volver") }
            }
            Text(
                "Cloro libre en mg/L. Los datos se guardan en este dispositivo y quedan pendientes de sincronización.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { mostrarFormulario = true }) { Text("Registrar medición") }
            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }

            if (state.cargando) {
                Text("Cargando historial...")
            } else if (state.mediciones.isEmpty()) {
                Text("Aún no hay mediciones para este punto.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.mediciones, key = Medicion::id) { measurement ->
                        MedicionItem(measurement)
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        NuevaMedicionDialog(
            onDismiss = { mostrarFormulario = false },
            onSave = { value ->
                historialViewModel.registrarMedicion(puntoId, value) { saved ->
                    mostrarFormulario = false
                    mensaje = if (saved) {
                        "Medición guardada localmente."
                    } else {
                        "No se pudo guardar. Revisa el rango permitido (0 a 20 mg/L) y tu acceso al punto."
                    }
                }
            }
        )
    }
}

@Composable
private fun MedicionItem(medicion: Medicion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${formatearNivel(medicion.nivelCloro)} mg/L", style = MaterialTheme.typography.titleMedium)
            Text("Estado: ${medicion.estado.name.lowercase().replaceFirstChar(Char::uppercase)}")
            medicion.direccion?.let {
                Text(
                    if (it == DireccionDesviacion.INSUFICIENTE) "Cloro insuficiente" else "Cloro excesivo"
                )
            }
            Text(medicion.fechaHora, style = MaterialTheme.typography.bodySmall)
            if (!medicion.sincronizada) {
                Text("Pendiente de sincronización", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun NuevaMedicionDialog(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var rawValue by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva medición") },
        text = {
            OutlinedTextField(
                value = rawValue,
                onValueChange = { rawValue = it.replace(',', '.'); error = null },
                label = { Text("Cloro libre (mg/L)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
        },
        confirmButton = {
            Button(onClick = {
                val value = rawValue.toDoubleOrNull()
                if (value == null || !value.isFinite() || value !in 0.0..20.0) {
                    error = "Ingresa un valor entre 0 y 20 mg/L."
                } else {
                    onSave(value)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
