package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun ResumenScreen(
    usuarioViewModel: UsuarioViewModel,
    onIrAlInicio: () -> Unit
) {
    val usuario by usuarioViewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Resumen del registro",
                style = MaterialTheme.typography.headlineMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatoResumen(etiqueta = "Nombre", valor = usuario.nombre)
                    DatoResumen(etiqueta = "Correo", valor = usuario.correo)
                    DatoResumen(etiqueta = "Contraseña", valor = "********")
                    DatoResumen(etiqueta = "Dirección", valor = usuario.direccion)
                    DatoResumen(
                        etiqueta = "Rol",
                        valor = when (usuario.rol) {
                            com.example.medicindecloroenlnea.domain.model.RolUsuario.OPERARIO -> "Operario"
                            com.example.medicindecloroenlnea.domain.model.RolUsuario.SUPERVISOR -> "Supervisor"
                            com.example.medicindecloroenlnea.domain.model.RolUsuario.JEFATURA -> "Jefatura"
                        }
                    )
                    DatoResumen(
                        etiqueta = "Granjas asignadas",
                        valor = usuario.granjasIds.joinToString { "Granja $it" }
                    )
                }
            }
            Button(
                onClick = onIrAlInicio,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir al menú de mediciones")
            }
        }
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.titleSmall)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}
