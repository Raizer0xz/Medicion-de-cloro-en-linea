package com.example.medicindecloroenlnea

import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.ValidacionUsuario
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class ValidacionUsuarioTest {
    @Test
    fun aceptaCuentaConDatosValidos() {
        assertFalse(ValidacionUsuario.validar(usuarioValido()).tieneErrores())
    }

    @Test
    fun identificaCamposObligatoriosYFaltaDeAsignacion() {
        val errors = ValidacionUsuario.validar(UsuarioUiState(granjasIds = emptyList()))

        assertNotNull(errors.nombre)
        assertNotNull(errors.correo)
        assertNotNull(errors.clave)
        assertNotNull(errors.direccion)
        assertNotNull(errors.granjas)
        assertNotNull(errors.aceptaTerminos)
    }

    @Test
    fun corregirLosCamposEliminaLosErrores() {
        val errors = ValidacionUsuario.validar(usuarioValido())

        assertNull(errors.nombre)
        assertNull(errors.correo)
        assertNull(errors.clave)
        assertNull(errors.direccion)
        assertNull(errors.granjas)
        assertNull(errors.aceptaTerminos)
    }

    private fun usuarioValido() = UsuarioUiState(
        nombre = "Usuario de prueba",
        correo = "usuario@example.com",
        clave = "claveficticia",
        direccion = "Dirección de prueba",
        aceptaTerminos = true
    )
}
