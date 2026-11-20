package com.example.medicindecloroenlnea.domain.model

object ValidacionFormulario {
    const val MENSAJE_MEDICION_INVALIDA = "Ingresa un valor entre 0 y 20 mg/L."
    const val MENSAJE_ACCION_VACIA = "Describe la acción correctiva."

    fun validarMedicion(valor: String): Double? =
        valor.replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() && it in 0.0..20.0 }

    fun validarAccion(descripcion: String): Boolean = descripcion.isNotBlank()
}
