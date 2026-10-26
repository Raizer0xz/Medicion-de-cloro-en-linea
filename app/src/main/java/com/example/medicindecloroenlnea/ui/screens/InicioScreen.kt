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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel

@Composable
fun InicioScreen(
    usuarioViewModel: UsuarioViewModel,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by usuarioViewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Inicio",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Medición de cloro en línea",
                style = MaterialTheme.typography.titleLarge
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sesión iniciada",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Button(
                onClick = onVerPerfil,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver resumen de mi cuenta")
            }
            Text(
                text = "El menú de mediciones, alertas e historial se incorporará en las próximas guías.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
