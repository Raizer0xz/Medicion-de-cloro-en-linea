package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.data.local.AlertaLocal
import com.example.medicindecloroenlnea.domain.model.Accion
import com.example.medicindecloroenlnea.domain.model.DireccionDesviacion
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.formatearNivel
import com.example.medicindecloroenlnea.ui.viewmodels.AlertasViewModel

@Composable
fun AlertasScreen(
    usuario: UsuarioUiState,
    alertasViewModel: AlertasViewModel = viewModel()
) {
    val state by alertasViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(usuario.id, usuario.rol, usuario.granjasIds) {
        alertasViewModel.configurarUsuario(
            usuario.id,
            usuario.nombre,
            usuario.rol,
            usuario.granjasIds
        )
    }

    when {
        state.cargando -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        }

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.pendientesSincronizacion > 0) {
                item {
                    Text(
                        "${state.pendientesSincronizacion} mediciones guardadas localmente pendientes de sincronización.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (state.alertas.isEmpty()) {
                item {
                    Text(
                        "No hay alertas para las granjas asignadas.",
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(state.alertas, key = AlertaLocal::id) { alert ->
                    AlertaCard(alert, state.rol, alertasViewModel)
                }
            }
        }
    }
}

@Composable
private fun AlertaCard(
    alerta: AlertaLocal,
    rol: RolUsuario,
    viewModel: AlertasViewModel
) {
    val color = colorDeEstado(alerta.estado)
    var mostrarFormulario by rememberSaveable(alerta.id) { mutableStateOf(false) }
    var mensaje by rememberSaveable(alerta.id) { mutableStateOf<String?>(null) }
    val acciones by viewModel.obtenerAcciones(alerta.id).collectAsState(initial = emptyList())
    val puedeCerrar = rol != RolUsuario.OPERARIO &&
        !alerta.atendida && alerta.tieneAccion && alerta.ultimaMedicionNormal

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (alerta.estado == EstadoCloro.CRITICO) "ALERTA CRÍTICA" else "Advertencia",
                color = color,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Punto: ${alerta.puntoNombre}")
            Text(
                "Cloro ${if (alerta.direccion == DireccionDesviacion.INSUFICIENTE) "insuficiente" else "excesivo"}: " +
                    "${formatearNivel(alerta.valor)} mg/L"
            )
            Text("Fecha: ${alerta.fechaHora}")
            Text(
                if (alerta.atendida) "Cerrada por ${alerta.cerradaPor ?: "supervisión"}"
                else "Abierta"
            )
            if (acciones.isNotEmpty()) {
                Text("Acciones correctivas", style = MaterialTheme.typography.titleSmall)
                acciones.forEach { action -> AccionItem(action) }
            }
            if (!alerta.atendida) {
                TextButton(onClick = { mostrarFormulario = true }) {
                    Text("Registrar acción correctiva")
                }
            }
            if (!alerta.atendida && rol != RolUsuario.OPERARIO && alerta.tieneAccion) {
                if (alerta.ultimaMedicionNormal) {
                    Button(onClick = {
                        viewModel.cerrarAlerta(alerta.id) { closed ->
                            mensaje = if (closed) "Alerta cerrada." else "No se pudo cerrar la alerta."
                        }
                    }) {
                        Text("Cerrar alerta")
                    }
                } else {
                    Text(
                        "Para cerrar, registra una medición de verificación normal desde el historial del punto.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        }
    }

    if (mostrarFormulario) {
        FormularioAccion(
            onDismiss = { mostrarFormulario = false },
            onSave = { description ->
                viewModel.registrarAccion(alerta.id, description) { saved ->
                    mostrarFormulario = false
                    mensaje = if (saved) "Acción registrada." else "No se pudo guardar la acción."
                }
            }
        )
    }
}

@Composable
private fun AccionItem(accion: Accion) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(accion.descripcion, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${accion.responsable} · ${accion.fechaHora} · ${accion.estado} · Medición #${accion.medicionId}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FormularioAccion(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acción correctiva") },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                supportingText = { Text("Se guardará con tu usuario y la fecha actual.") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(description) },
                enabled = description.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
