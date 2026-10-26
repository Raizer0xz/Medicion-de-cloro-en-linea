package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun PerfilScreen(usuarioViewModel: UsuarioViewModel) {
    val usuario by usuarioViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatoResumen(etiqueta = "Nombre", valor = usuario.nombre)
                DatoResumen(etiqueta = "Correo", valor = usuario.correo)
                DatoResumen(etiqueta = "Dirección", valor = usuario.direccion)
                DatoResumen(etiqueta = "Rol", valor = usuario.rol.name)
                DatoResumen(
                    etiqueta = "Granjas asignadas",
                    valor = usuario.granjasIds.joinToString().ifBlank { "Sin asignación" }
                )
            }
        }
        Text(
            "La cuenta se conserva en la base local de este dispositivo.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.titleSmall)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
