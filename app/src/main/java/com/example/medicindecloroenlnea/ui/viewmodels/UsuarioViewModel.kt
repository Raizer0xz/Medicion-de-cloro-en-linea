package com.example.medicindecloroenlnea.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.medicindecloroenlnea.domain.model.UsuarioErrores
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsuarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    private val _errores = MutableStateFlow(UsuarioErrores())
    val errores: StateFlow<UsuarioErrores> = _errores.asStateFlow()

    private var intentoDeEnvio = false

    fun actualizarNombre(nombre: String) = actualizarEstado { it.copy(nombre = nombre) }

    fun actualizarCorreo(correo: String) = actualizarEstado { it.copy(correo = correo) }

    fun actualizarClave(clave: String) = actualizarEstado { it.copy(clave = clave) }

    fun actualizarDireccion(direccion: String) =
        actualizarEstado { it.copy(direccion = direccion) }

    fun actualizarAceptaTerminos(acepta: Boolean) =
        actualizarEstado { it.copy(aceptaTerminos = acepta) }

    fun validarFormulario(): Boolean {
        intentoDeEnvio = true
        val usuario = _uiState.value
        val errores = UsuarioErrores(
            nombre = if (usuario.nombre.isBlank()) "Ingresa tu nombre." else null,
            correo = if (!CORREO_VALIDO.matches(usuario.correo.trim())) {
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
            aceptaTerminos = if (!usuario.aceptaTerminos) {
                "Confirma que aceptas los términos."
            } else {
                null
            }
        )
        _errores.value = errores
        return !errores.tieneErrores()
    }

    private fun actualizarEstado(transform: (UsuarioUiState) -> UsuarioUiState) {
        _uiState.update(transform)
        if (intentoDeEnvio) {
            validarFormulario()
        }
    }

    private companion object {
        const val LONGITUD_MINIMA_CLAVE = 8
        val CORREO_VALIDO = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
