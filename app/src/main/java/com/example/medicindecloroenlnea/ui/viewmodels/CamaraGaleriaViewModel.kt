package com.example.medicindecloroenlnea.ui.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.medicindecloroenlnea.domain.model.ImagenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CamaraGaleriaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImagenUiState())
    val uiState = _uiState.asStateFlow()

    fun imagenDesdeCamara(bitmap: Bitmap?) {
        if (bitmap == null) return
        _uiState.value = ImagenUiState(bitmap = bitmap, origen = "Cámara")
    }

    fun imagenDesdeGaleria(uri: Uri?) {
        if (uri == null) return
        _uiState.value = ImagenUiState(uri = uri, origen = "Galería")
    }

    fun limpiarImagen() {
        _uiState.value = ImagenUiState()
    }
}
