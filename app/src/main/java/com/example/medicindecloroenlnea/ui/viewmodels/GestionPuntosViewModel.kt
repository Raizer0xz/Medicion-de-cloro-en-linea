package com.example.medicindecloroenlnea.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.GestionPuntosRepository
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.UsuarioUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestionPuntosUiState(
    val granjas: List<Granja> = emptyList(),
    val puntos: List<PuntoMedicion> = emptyList(),
    val cargando: Boolean = true,
    val sinPermiso: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class GestionPuntosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GestionPuntosRepository(application)
    private val _uiState = MutableStateFlow(GestionPuntosUiState())
    val uiState: StateFlow<GestionPuntosUiState> = _uiState.asStateFlow()

    private var jobDatos: Job? = null
    private var usuario = UsuarioUiState()

    fun configurar(usuarioActual: UsuarioUiState) {
        if (usuario == usuarioActual && jobDatos != null) return
        usuario = usuarioActual
        jobDatos?.cancel()
        val puedeAdministrar =
            usuarioActual.id != null &&
                (usuarioActual.rol == RolUsuario.SUPERVISOR ||
                    usuarioActual.rol == RolUsuario.JEFATURA)
        if (!puedeAdministrar) {
            _uiState.value = GestionPuntosUiState(cargando = false, sinPermiso = true)
            return
        }

        val granjasPermitidas = usuarioActual.granjasIds.distinct()
        _uiState.value = GestionPuntosUiState(cargando = true)
        jobDatos = viewModelScope.launch {
            combine(
                repository.observarGranjas(granjasPermitidas),
                repository.observarPuntos(granjasPermitidas)
            ) { farms, points ->
                _uiState.update {
                    it.copy(granjas = farms, puntos = points, cargando = false)
                }
            }.collect {}
        }
    }

    fun guardar(
        puntoId: Int?,
        granjaId: Int,
        nombre: String,
        codigo: String,
        galpon: String,
        tipo: String,
        nivelInicialTexto: String,
        onCompleted: (Boolean) -> Unit
    ) {
        val nivelInicial = if (puntoId == null) nivelInicialTexto.replace(',', '.').toDoubleOrNull() else 0.0
        if (puntoId == null && (nivelInicial == null || !nivelInicial.isFinite() || nivelInicial !in 0.0..20.0)) {
            _uiState.update {
                it.copy(error = "Ingresa un nivel inicial entre 0 y 20 mg/L.", mensaje = null)
            }
            onCompleted(false)
            return
        }

        viewModelScope.launch {
            val result = if (puntoId == null) {
                repository.crearPunto(
                    usuario.id,
                    usuario.rol,
                    usuario.granjasIds,
                    granjaId,
                    nombre,
                    codigo,
                    galpon,
                    tipo,
                    requireNotNull(nivelInicial)
                )
            } else {
                repository.actualizarPunto(
                    usuario.id,
                    usuario.rol,
                    usuario.granjasIds,
                    puntoId,
                    nombre,
                    codigo,
                    galpon,
                    tipo
                )
            }
            _uiState.update {
                it.copy(
                    mensaje = result.mensaje.takeIf { result.exitoso },
                    error = result.mensaje.takeUnless { result.exitoso }
                )
            }
            onCompleted(result.exitoso)
        }
    }

    fun eliminar(puntoId: Int) {
        viewModelScope.launch {
            val result = repository.eliminarPunto(
                usuario.id,
                usuario.rol,
                usuario.granjasIds,
                puntoId
            )
            _uiState.update {
                it.copy(
                    mensaje = result.mensaje.takeIf { result.exitoso },
                    error = result.mensaje.takeUnless { result.exitoso }
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensaje = null, error = null) }
    }
}
