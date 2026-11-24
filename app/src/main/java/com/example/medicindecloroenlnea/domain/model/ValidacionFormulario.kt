package com.example.medicindecloroenlnea.domain.model

object ValidacionFormulario {
    const val MENSAJE_MEDICION_INVALIDA = "Ingresa un valor entre 0 y 20 mg/L."
    const val MENSAJE_ACCION_VACIA = "Describe la acción correctiva."
    const val MENSAJE_CORREO_INVALIDO = "Ingresa un correo electrónico válido."
    const val MENSAJE_CLAVE_VACIA = "Ingresa tu contraseña."
    private val correoValido =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarCorreo(correo: String): Boolean = correoValido.matches(correo.trim())

    fun validarClave(clave: String): Boolean = clave.isNotBlank()

    fun validarMedicion(valor: String): Double? =
        valor.replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() && it in 0.0..20.0 }

    fun validarAccion(descripcion: String): Boolean = descripcion.isNotBlank()
}
