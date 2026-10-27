package com.example.medicindecloroenlnea.domain.model

import android.graphics.Bitmap
import android.net.Uri

data class ImagenUiState(
    val bitmap: Bitmap? = null,
    val uri: Uri? = null,
    val origen: String = ""
)
