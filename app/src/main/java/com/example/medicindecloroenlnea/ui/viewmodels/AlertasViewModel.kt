package com.example.medicindecloroenlnea.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.local.AlertaLocal
import com.example.medicindecloroenlnea.data.repository.LocalCloroRepository
import com.example.medicindecloroenlnea.domain.model.Accion
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AlertasUiState(
    val alertas: List<AlertaLocal> = emptyList(),
    val rol: RolUsuario = RolUsuario.OPERARIO,
    val pendientesSincronizacion: Int = 0,
    val cargando: Boolean = true
)

class AlertasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalCloroRepository(application)
    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState: StateFlow<AlertasUiState> = _uiState.asStateFlow()

    private var usuarioId: Long? = null
    private var nombreUsuario: String = ""
    private var granjasPermitidas: List<Int> = emptyList()
    private var jobAlertas: Job? = null
    private var jobPendientes: Job? = null

    fun configurarUsuario(
        id: Long?,
        nombre: String,
        rol: RolUsuario,
        granjas: List<Int>
    ) {
        usuarioId = id
        nombreUsuario = nombre
        granjasPermitidas = granjas
        jobAlertas?.cancel()
        jobPendientes?.cancel()
        _uiState.update { it.copy(rol = rol, cargando = true) }
        jobAlertas = viewModelScope.launch {
            repository.obtenerAlertasLocales().collect { alerts ->
                _uiState.update {
                    it.copy(
                        alertas = alerts.filter { alert -> alert.granjaId in granjas },
                        cargando = false
                    )
                }
            }
        }
        jobPendientes = viewModelScope.launch {
            repository.obtenerPendientesSincronizacion(granjas).collect { count ->
                _uiState.update { it.copy(pendientesSincronizacion = count) }
            }
        }
    }

    fun obtenerAcciones(alertaId: Long): Flow<List<Accion>> = repository.obtenerAcciones(alertaId)

    fun registrarAccion(alertaId: Long, descripcion: String, onCompleted: (Boolean) -> Unit) {
        val id = usuarioId ?: return onCompleted(false)
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                repository.registrarAccion(
                    alertaId,
                    descripcion,
                    nombreUsuario,
                    id,
                    granjasPermitidas
                )
            }
            onCompleted(saved)
        }
    }

    fun cerrarAlerta(alertaId: Long, onCompleted: (Boolean) -> Unit) {
        val id = usuarioId ?: return onCompleted(false)
        viewModelScope.launch {
            val closed = withContext(Dispatchers.IO) { repository.cerrarAlerta(alertaId, id) }
            onCompleted(closed)
        }
    }
}
