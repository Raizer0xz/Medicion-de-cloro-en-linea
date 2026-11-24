package com.example.medicindecloroenlnea

import com.example.medicindecloroenlnea.domain.model.ValidacionFormulario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionFormularioTest {
    @Test
    fun validaMedicionesEnLimitesYConSeparadorDecimalLocal() {
        assertEquals(0.0, ValidacionFormulario.validarMedicion("0")!!, 0.0)
        assertEquals(12.5, ValidacionFormulario.validarMedicion("12,5")!!, 0.0)
        assertEquals(20.0, ValidacionFormulario.validarMedicion("20")!!, 0.0)
    }

    @Test
    fun rechazaMedicionesNoNumericasNoFinitasOFueraDeRango() {
        listOf("", "abc", "NaN", "Infinity", "-0.1", "20.1").forEach { input ->
            assertNull("Entrada inválida: $input", ValidacionFormulario.validarMedicion(input))
        }
    }

    @Test
    fun requiereUnaDescripcionConContenidoParaLaAccion() {
        assertFalse(ValidacionFormulario.validarAccion(" \n "))
        assertTrue(ValidacionFormulario.validarAccion("Limpieza del sistema"))
    }

    @Test
    fun validaCorreoYContrasenaDelInicioDeSesionPorSeparado() {
        assertTrue(ValidacionFormulario.validarCorreo(" persona@example.com "))
        assertFalse(ValidacionFormulario.validarCorreo("correo-invalido"))
        assertTrue(ValidacionFormulario.validarClave("clave"))
        assertFalse(ValidacionFormulario.validarClave("  "))
    }
}
