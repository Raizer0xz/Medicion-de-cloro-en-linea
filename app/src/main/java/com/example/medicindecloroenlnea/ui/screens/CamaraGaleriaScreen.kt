package com.example.medicindecloroenlnea.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.medicindecloroenlnea.ui.viewmodels.CamaraGaleriaViewModel

@Composable
fun CamaraGaleriaScreen(
    modifier: Modifier = Modifier,
    viewModel: CamaraGaleriaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
        viewModel::imagenDesdeCamara
    )
    val gallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
        viewModel::imagenDesdeGaleria
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Foto de perfil", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { camera.launch(null) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Tomar fotografía")
            }
            OutlinedButton(
                onClick = { gallery.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Elegir imagen")
            }
        }

        when {
            state.bitmap != null -> Image(
                bitmap = state.bitmap!!.asImageBitmap(),
                contentDescription = "Fotografía de perfil",
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Fit
            )

            state.uri != null -> AsyncImage(
                model = state.uri,
                contentDescription = "Imagen de perfil seleccionada",
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Fit
            )

            else -> Text("No hay ninguna imagen seleccionada.")
        }

        if (state.origen.isNotEmpty()) {
            Text("Imagen obtenida desde: ${state.origen}")
            TextButton(
                onClick = viewModel::limpiarImagen,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Limpiar imagen")
            }
        }
    }
}
