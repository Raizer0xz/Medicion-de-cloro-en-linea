package com.example.medicindecloroenlnea.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.LocalCloroRepository
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.UsuarioErrores
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import com.example.medicindecloroenlnea.domain.model.ValidacionUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalCloroRepository(application)
    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    private val _errores = MutableStateFlow(UsuarioErrores())
    val errores: StateFlow<UsuarioErrores> = _errores.asStateFlow()

    private var intentoDeEnvio = false

    fun actualizarNombre(nombre: String) = actualizarEstado { it.copy(nombre = nombre) }
    fun actualizarCorreo(correo: String) = actualizarEstado { it.copy(correo = correo) }
    fun actualizarClave(clave: String) = actualizarEstado { it.copy(clave = clave) }
    fun actualizarDireccion(direccion: String) = actualizarEstado { it.copy(direccion = direccion) }
    fun actualizarRol(rol: RolUsuario) = actualizarEstado { it.copy(rol = rol) }
    fun actualizarAceptaTerminos(acepta: Boolean) =
        actualizarEstado { it.copy(aceptaTerminos = acepta) }

    fun alternarGranja(granjaId: Int) = actualizarEstado { user ->
        val selected = if (granjaId in user.granjasIds) {
            user.granjasIds - granjaId
        } else {
            user.granjasIds + granjaId
        }
        user.copy(granjasIds = selected)
    }

    fun validarFormulario(): Boolean {
        intentoDeEnvio = true
        val errors = ValidacionUsuario.validar(_uiState.value)
        _errores.value = errors
        return !errors.tieneErrores()
    }

    fun registrarUsuario(onCompleted: (Boolean) -> Unit) {
        if (!validarFormulario()) {
            onCompleted(false)
            return
        }
        val user = _uiState.value
        viewModelScope.launch {
            val savedUser = withContext(Dispatchers.IO) {
                repository.registrarUsuario(
                    user.nombre,
                    user.correo,
                    user.direccion,
                    user.clave,
                    user.rol,
                    user.granjasIds
                )
            }
            if (savedUser != null) cargarPerfil(savedUser)
            onCompleted(savedUser != null)
        }
    }

    fun iniciarSesion(correo: String, clave: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                repository.autenticar(correo, clave)
            }
            if (user != null) cargarPerfil(user)
            onCompleted(user != null)
        }
    }

    fun cerrarSesion() {
        _uiState.value = UsuarioUiState()
        _errores.value = UsuarioErrores()
        intentoDeEnvio = false
    }

    private fun cargarPerfil(user: com.example.medicindecloroenlnea.data.local.UsuarioAutenticado) {
        _uiState.update {
            it.copy(
                id = user.id,
                nombre = user.nombre,
                correo = user.correo,
                clave = "",
                direccion = user.direccion,
                rol = user.rol,
                granjasIds = user.granjasIds
            )
        }
    }

    private fun actualizarEstado(transform: (UsuarioUiState) -> UsuarioUiState) {
        _uiState.update(transform)
        if (intentoDeEnvio) validarFormulario()
    }

}
