package com.example.medicindecloroenlnea.domain.model

object ValidacionUsuario {
    private const val LONGITUD_MINIMA_CLAVE = 8
    private val correoValido =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validar(usuario: UsuarioUiState): UsuarioErrores = UsuarioErrores(
        nombre = if (usuario.nombre.isBlank()) "Ingresa tu nombre." else null,
        correo = if (!correoValido.matches(usuario.correo.trim())) {
            "Ingresa un correo electrónico válido."
        } else {
            null
        },
        clave = if (usuario.clave.length < LONGITUD_MINIMA_CLAVE) {
            "La contraseña debe tener al menos $LONGITUD_MINIMA_CLAVE caracteres."
        } else {
            null
        },
        direccion = if (usuario.direccion.isBlank()) "Ingresa tu dirección." else null,
        granjas = if (usuario.granjasIds.isEmpty()) "Selecciona al menos una granja." else null,
        aceptaTerminos = if (!usuario.aceptaTerminos) {
            "Confirma que aceptas los términos."
        } else {
            null
        }
    )
}
