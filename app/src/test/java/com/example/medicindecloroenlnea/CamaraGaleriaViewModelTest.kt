package com.example.medicindecloroenlnea

import com.example.medicindecloroenlnea.ui.viewmodels.CamaraGaleriaViewModel
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CamaraGaleriaViewModelTest {
    @Test
    fun iniciaSinImagenSeleccionada() {
        val viewModel = CamaraGaleriaViewModel()

        assertNull(viewModel.uiState.value.bitmap)
        assertNull(viewModel.uiState.value.uri)
        assertTrue(viewModel.uiState.value.origen.isEmpty())
    }

    @Test
    fun cancelarLaCapturaNoReemplazaElEstadoDeImagen() {
        val viewModel = CamaraGaleriaViewModel()

        viewModel.imagenDesdeCamara(null)
        viewModel.imagenDesdeGaleria(null)

        assertNull(viewModel.uiState.value.bitmap)
        assertNull(viewModel.uiState.value.uri)
        assertTrue(viewModel.uiState.value.origen.isEmpty())
    }

    @Test
    fun limpiarImagenDejaElEstadoInicial() {
        val viewModel = CamaraGaleriaViewModel()

        viewModel.limpiarImagen()

        assertNull(viewModel.uiState.value.bitmap)
        assertNull(viewModel.uiState.value.uri)
        assertTrue(viewModel.uiState.value.origen.isEmpty())
    }
}
