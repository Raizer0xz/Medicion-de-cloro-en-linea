package com.example.medicindecloroenlnea.domain.model

data class UsuarioUiState(
    val id: Long? = null,
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val direccion: String = "",
    val aceptaTerminos: Boolean = false,
    val rol: RolUsuario = RolUsuario.OPERARIO,
    val granjasIds: List<Int> = listOf(1)
)
