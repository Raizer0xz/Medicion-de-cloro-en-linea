package com.example.medicindecloroenlnea.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.LocalCloroRepository
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GranjasUiState(
    val granjas: List<Granja> = emptyList(),
    val puntos: List<PuntoMedicion> = emptyList(),
    val granjaSeleccionada: Int? = null,
    val pendientesSincronizacion: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null
)

class GranjasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalCloroRepository(application)
    private val _uiState = MutableStateFlow(GranjasUiState())
    val uiState: StateFlow<GranjasUiState> = _uiState.asStateFlow()

    private var jobGranjas: Job? = null
    private var jobPuntos: Job? = null
    private var jobPendientes: Job? = null
    private var usuarioId: Long? = null
    private var granjasPermitidas: List<Int> = emptyList()

    fun configurarAcceso(granjaIds: List<Int>, idUsuario: Long?) {
        if (granjaIds == granjasPermitidas && idUsuario == usuarioId && jobGranjas != null) return
        granjasPermitidas = granjaIds
        usuarioId = idUsuario
        jobGranjas?.cancel()
        jobPuntos?.cancel()
        jobPendientes?.cancel()
        _uiState.value = GranjasUiState()
        jobGranjas = viewModelScope.launch {
            repository.obtenerGranjas(granjaIds).collect { farms ->
                _uiState.update { it.copy(granjas = farms, cargando = false) }
                if (_uiState.value.granjaSeleccionada !in farms.map(Granja::id)) {
                    farms.firstOrNull()?.let { seleccionarGranja(it.id) }
                }
            }
        }
        jobPendientes = viewModelScope.launch {
            repository.obtenerPendientesSincronizacion(granjasPermitidas).collect { count ->
                _uiState.update { it.copy(pendientesSincronizacion = count) }
            }
        }
    }

    fun seleccionarGranja(granjaId: Int) {
        if (granjaId !in granjasPermitidas) return
        _uiState.update { it.copy(granjaSeleccionada = granjaId, cargando = true) }
        jobPuntos?.cancel()
        jobPuntos = viewModelScope.launch {
            repository.obtenerPuntos(granjaId, granjasPermitidas).collect { points ->
                _uiState.update { it.copy(puntos = points, cargando = false) }
            }
        }
    }

    fun registrarMedicion(puntoId: Int, valor: Double, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                repository.registrarMedicion(puntoId, valor, usuarioId, granjasPermitidas)
            }
            _uiState.update {
                it.copy(
                    error = if (saved) null else "No se pudo guardar la medición. Verifica tu acceso al punto y el valor ingresado."
                )
            }
            onCompleted(saved)
        }
    }
}
