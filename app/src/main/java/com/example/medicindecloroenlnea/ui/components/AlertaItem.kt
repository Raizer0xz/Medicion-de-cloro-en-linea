package com.example.medicindecloroenlnea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicindecloroenlnea.domain.model.Alerta
import com.example.medicindecloroenlnea.ui.screens.colorDeEstado

@Composable
fun AlertaItem(alerta: Alerta, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(14.dp).background(colorDeEstado(alerta.estado), CircleShape))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Punto ${alerta.puntoId}", style = MaterialTheme.typography.titleMedium)
                Text("Cloro: ${alerta.nivelCloro} mg/L", style = MaterialTheme.typography.bodyMedium)
                Text("Hora: ${alerta.fechaHora}", style = MaterialTheme.typography.bodySmall)
                Text(
                    if (alerta.atendida) "Atendida" else "Pendiente",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}