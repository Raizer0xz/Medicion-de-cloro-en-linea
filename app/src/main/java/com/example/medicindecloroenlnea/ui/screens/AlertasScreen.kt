package com.example.medicindecloroenlnea.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.medicindecloroenlnea.data.local.AlertaLocal
import com.example.medicindecloroenlnea.domain.model.Accion
import com.example.medicindecloroenlnea.domain.model.DireccionDesviacion
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.ValidacionFormulario
import com.example.medicindecloroenlnea.domain.model.formatearNivel
import com.example.medicindecloroenlnea.ui.components.CampoValidado
import com.example.medicindecloroenlnea.ui.viewmodels.AlertasViewModel
import java.io.File

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
            onGuardarEvidenciaUri = { uri, onCompleted ->
                viewModel.guardarEvidencia(uri = uri, onCompleted = onCompleted)
            },
            onGuardarEvidenciaBitmap = { bitmap, onCompleted ->
                viewModel.guardarEvidencia(bitmap = bitmap, onCompleted = onCompleted)
            },
            onSave = { description, fotoUri ->
                viewModel.registrarAccion(alerta.id, description, fotoUri) { saved ->
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
        accion.fotoUri?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "Evidencia fotográfica de la acción correctiva",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(220.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun FormularioAccion(
    onDismiss: () -> Unit,
    onGuardarEvidenciaUri: (Uri, (String?, String?) -> Unit) -> Unit,
    onGuardarEvidenciaBitmap: (Bitmap, (String?, String?) -> Unit) -> Unit,
    onSave: (String, String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var attemptedSave by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var errorEvidencia by remember { mutableStateOf<String?>(null) }
    var guardandoEvidencia by remember { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            guardandoEvidencia = true
            onGuardarEvidenciaBitmap(bitmap) { path, error ->
                fotoUri = path
                errorEvidencia = error
                guardandoEvidencia = false
            }
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            guardandoEvidencia = true
            onGuardarEvidenciaUri(uri) { path, error ->
                fotoUri = path
                errorEvidencia = error
                guardandoEvidencia = false
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acción correctiva") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoValidado(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    leadingIcon = Icons.Default.Edit,
                    errorMessage = if (
                        attemptedSave && !ValidacionFormulario.validarAccion(description)
                    ) {
                        ValidacionFormulario.MENSAJE_ACCION_VACIA
                    } else {
                        null
                    },
                    supportingMessage = "Se guardará con tu usuario y la fecha actual.",
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Evidencia fotográfica (opcional)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { camera.launch(null) },
                            enabled = !guardandoEvidencia,
                            modifier = Modifier.weight(1f)
                        ) { Text("Tomar foto", maxLines = 1) }
                        OutlinedButton(
                            onClick = { gallery.launch("image/*") },
                            enabled = !guardandoEvidencia,
                            modifier = Modifier.weight(1f)
                        ) { Text("Elegir foto", maxLines = 1) }
                    }
                    fotoUri?.let { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Evidencia fotográfica seleccionada",
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (guardandoEvidencia) Text("Guardando fotografía…")
                    errorEvidencia?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attemptedSave = true
                    if (ValidacionFormulario.validarAccion(description) && !guardandoEvidencia) {
                        onSave(description.trim(), fotoUri)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
