package com.example.medicindecloroenlnea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicindecloroenlnea.domain.model.Alerta
import com.example.medicindecloroenlnea.navigation.AppRoutes
import com.example.medicindecloroenlnea.ui.viewmodels.AlertasViewModel
import com.example.medicindecloroenlnea.ui.viewmodels.MainViewModel

private data class Pestana(
    val etiqueta: String,
    val ruta: AppRoutes,
    val icono: ImageVector
)

private val pestanas = listOf(
    Pestana("Puntos", AppRoutes.Puntos, Icons.Default.Home),
    Pestana("Alertas", AppRoutes.Alertas, Icons.Default.Notifications),
    Pestana("Resumen", AppRoutes.Resumen, Icons.Default.Info)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    mainViewModel: MainViewModel,
    alertasViewModel: AlertasViewModel = viewModel()
) {
    val uiState by alertasViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Alertas") }) },
        bottomBar = {
            NavigationBar {
                pestanas.forEach { pestana ->
                    NavigationBarItem(
                        selected = pestana.ruta == AppRoutes.Alertas,
                        onClick = {
                            mainViewModel.navigateTo(pestana.ruta, singleTop = true)
                        },
                        icon = { Icon(pestana.icono, contentDescription = pestana.etiqueta) },
                        label = { Text(pestana.etiqueta) }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.alertas) { alerta ->
                AlertaItem(alerta)
            }
        }
    }
}

// Si algo sale en rojo, es aquí: depende de los campos reales de Alerta
@Composable
private fun AlertaItem(alerta: Alerta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorDeEstado(alerta.estado))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nivel de cloro: ${alerta.nivelCloro} mg/L")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hora: ${alerta.fechaHora.replace("T", " ")}")
                Text(if (alerta.atendida) "Atendida" else "Pendiente")
            }
        }
    }
}