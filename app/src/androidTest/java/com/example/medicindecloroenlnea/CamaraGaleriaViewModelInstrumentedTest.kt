package com.example.medicindecloroenlnea

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medicindecloroenlnea.ui.viewmodels.CamaraGaleriaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CamaraGaleriaViewModelInstrumentedTest {
    @Test
    fun seleccionaImagenDeCamaraGaleriaYPermiteLimpiarla() {
        val viewModel = CamaraGaleriaViewModel()
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val uri = Uri.parse("content://media/external/images/13")

        viewModel.imagenDesdeCamara(bitmap)
        assertSame(bitmap, viewModel.uiState.value.bitmap)
        assertNull(viewModel.uiState.value.uri)
        assertEquals("Cámara", viewModel.uiState.value.origen)

        viewModel.imagenDesdeGaleria(uri)
        assertNull(viewModel.uiState.value.bitmap)
        assertEquals(uri, viewModel.uiState.value.uri)
        assertEquals("Galería", viewModel.uiState.value.origen)

        viewModel.limpiarImagen()
        assertNull(viewModel.uiState.value.bitmap)
        assertNull(viewModel.uiState.value.uri)
        assertEquals("", viewModel.uiState.value.origen)
    }
}
