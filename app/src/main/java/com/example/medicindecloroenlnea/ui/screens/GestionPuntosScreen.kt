package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.formatearNivel
import com.example.medicindecloroenlnea.ui.viewmodels.GestionPuntosViewModel

@Composable
fun GestionPuntosScreen(
    usuario: UsuarioUiState,
    onVolver: () -> Unit,
    viewModel: GestionPuntosViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var puntoEditando by remember { mutableStateOf<PuntoMedicion?>(null) }
    var puntoAEliminar by remember { mutableStateOf<PuntoMedicion?>(null) }
    var granjaSeleccionada by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(usuario) {
        viewModel.configurar(usuario)
    }
    LaunchedEffect(state.granjas) {
        if (granjaSeleccionada !in state.granjas.map(Granja::id)) {
            granjaSeleccionada = state.granjas.firstOrNull()?.id
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Administrar puntos", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onVolver) { Text("Volver") }
            }

            if (state.sinPermiso) {
                Text(
                    "Solo supervisores y jefatura pueden administrar puntos.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    "Los puntos nuevos guardan su medición inicial en el historial.",
                    style = MaterialTheme.typography.bodyMedium
                )
                state.mensaje?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (state.cargando) {
                    Text("Cargando puntos...")
                } else if (state.granjas.isEmpty()) {
                    Text("No tienes granjas asignadas para administrar.")
                } else {
                    Button(
                        onClick = {
                            viewModel.limpiarMensajes()
                            puntoEditando = null
                            mostrarFormulario = true
                        }
                    ) {
                        Text("Registrar punto")
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.puntos, key = PuntoMedicion::id) { punto ->
                            PuntoAdministrableItem(
                                punto = punto,
                                granja = state.granjas.firstOrNull { it.id == punto.granjaId },
                                onEditar = {
                                    viewModel.limpiarMensajes()
                                    puntoEditando = punto
                                    mostrarFormulario = true
                                },
                                onEliminar = { puntoAEliminar = punto }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        PuntoFormDialog(
            punto = puntoEditando,
            granjas = state.granjas,
            granjaInicial = granjaSeleccionada,
            error = state.error,
            onDismiss = { mostrarFormulario = false },
            onGuardar = { puntoId, granjaId, nombre, codigo, galpon, tipo, nivel ->
                viewModel.guardar(
                    puntoId,
                    granjaId,
                    nombre,
                    codigo,
                    galpon,
                    tipo,
                    nivel
                ) { guardado ->
                    if (guardado) mostrarFormulario = false
                }
            }
        )
    }

    puntoAEliminar?.let { punto ->
        AlertDialog(
            onDismissRequest = { puntoAEliminar = null },
            title = { Text("Eliminar punto") },
            text = {
                Text(
                    "¿Eliminar ${punto.codigo} · ${punto.nombre}? " +
                        "Los puntos con mediciones históricas no se pueden eliminar."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(punto.id)
                    puntoAEliminar = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { puntoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun PuntoAdministrableItem(
    punto: PuntoMedicion,
    granja: Granja?,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("${punto.codigo} · ${punto.nombre}", style = MaterialTheme.typography.titleMedium)
            Text("${granja?.nombre ?: "Granja"} · ${punto.galpon} · ${punto.tipo}")
            Text("Nivel actual: ${formatearNivel(punto.nivelActual)} mg/L")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onEliminar) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun PuntoFormDialog(
    punto: PuntoMedicion?,
    granjas: List<Granja>,
    granjaInicial: Int?,
    error: String?,
    onDismiss: () -> Unit,
    onGuardar: (Int?, Int, String, String, String, String, String) -> Unit
) {
    var granjaId by rememberSaveable(punto?.id, granjaInicial) {
        mutableStateOf(punto?.granjaId ?: granjaInicial)
    }
    var nombre by rememberSaveable(punto?.id) { mutableStateOf(punto?.nombre.orEmpty()) }
    var codigo by rememberSaveable(punto?.id) { mutableStateOf(punto?.codigo.orEmpty()) }
    var galpon by rememberSaveable(punto?.id) { mutableStateOf(punto?.galpon.orEmpty()) }
    var tipo by rememberSaveable(punto?.id) { mutableStateOf(punto?.tipo.orEmpty()) }
    var nivelInicial by rememberSaveable(punto?.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (punto == null) "Registrar punto" else "Editar punto") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (punto == null) {
                    Text("Granja")
                    granjas.forEach { granja ->
                        FilterChip(
                            selected = granja.id == granjaId,
                            onClick = { granjaId = granja.id },
                            label = { Text(granja.nombre) }
                        )
                    }
                }
                CampoPunto(nombre, { nombre = it }, "Nombre")
                CampoPunto(codigo, { codigo = it }, "Código")
                CampoPunto(galpon, { galpon = it }, "Galpón o área")
                CampoPunto(tipo, { tipo = it }, "Tipo de punto")
                if (punto == null) {
                    OutlinedTextField(
                        value = nivelInicial,
                        onValueChange = { nivelInicial = it.replace(',', '.') },
                        label = { Text("Medición inicial (mg/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Se aplican los rangos predeterminados y se crea una alerta si corresponde.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(2.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled = granjaId != null,
                onClick = {
                    val selectedFarm = granjaId ?: return@TextButton
                    onGuardar(
                        punto?.id,
                        selectedFarm,
                        nombre,
                        codigo,
                        galpon,
                        tipo,
                        nivelInicial
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CampoPunto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
