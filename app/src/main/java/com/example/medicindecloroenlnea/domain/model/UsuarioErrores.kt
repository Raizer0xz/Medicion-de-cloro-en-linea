package com.example.medicindecloroenlnea.domain.model

data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val direccion: String? = null,
    val granjas: String? = null,
    val aceptaTerminos: String? = null
) {
    fun tieneErrores(): Boolean =
        nombre != null ||
            correo != null ||
            clave != null ||
            direccion != null ||
            granjas != null ||
            aceptaTerminos != null
}
