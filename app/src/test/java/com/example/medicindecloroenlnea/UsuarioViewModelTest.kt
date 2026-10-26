package com.example.medicindecloroenlnea

import com.example.medicindecloroenlnea.ui.viewmodels.UsuarioViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioViewModelTest {
    @Test
    fun validarFormularioConDatosValidos() {
        val viewModel = UsuarioViewModel()
        ingresarDatosValidos(viewModel)

        assertTrue(viewModel.validarFormulario())
        assertFalse(viewModel.errores.value.tieneErrores())
    }

    @Test
    fun validarFormularioActualizaErrores() {
        val viewModel = UsuarioViewModel()

        assertFalse(viewModel.validarFormulario())
        assertTrue(viewModel.errores.value.tieneErrores())
        assertTrue(viewModel.errores.value.nombre != null)
        assertTrue(viewModel.errores.value.correo != null)
        assertTrue(viewModel.errores.value.clave != null)
        assertTrue(viewModel.errores.value.direccion != null)
        assertTrue(viewModel.errores.value.aceptaTerminos != null)
    }

    @Test
    fun erroresSeLimpianAlCorregirLosCampos() {
        val viewModel = UsuarioViewModel()
        viewModel.validarFormulario()
        ingresarDatosValidos(viewModel)

        assertTrue(viewModel.validarFormulario())
        assertNull(viewModel.errores.value.nombre)
        assertNull(viewModel.errores.value.correo)
        assertNull(viewModel.errores.value.clave)
        assertNull(viewModel.errores.value.direccion)
        assertNull(viewModel.errores.value.aceptaTerminos)
    }

    private fun ingresarDatosValidos(viewModel: UsuarioViewModel) {
        viewModel.actualizarNombre("Usuario de prueba")
        viewModel.actualizarCorreo("usuario@example.com")
        viewModel.actualizarClave("claveficticia")
        viewModel.actualizarDireccion("Dirección de prueba")
        viewModel.actualizarAceptaTerminos(true)
    }
}
