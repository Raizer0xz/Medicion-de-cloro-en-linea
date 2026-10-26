package com.example.medicindecloroenlnea.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.LocalCloroRepository
import com.example.medicindecloroenlnea.domain.model.Medicion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HistorialUiState(
    val mediciones: List<Medicion> = emptyList(),
    val cargando: Boolean = true
)

class HistorialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalCloroRepository(application)
    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()
    private var jobMediciones: Job? = null
    private var puntoActual: Int? = null
    private var granjasPermitidas: List<Int> = emptyList()
    private var usuarioId: Long? = null

    fun configurar(puntoId: Int, idUsuario: Long?, granjas: List<Int>) {
        if (jobMediciones != null && puntoActual == puntoId &&
            usuarioId == idUsuario && granjasPermitidas == granjas
        ) return
        jobMediciones?.cancel()
        puntoActual = puntoId
        usuarioId = idUsuario
        granjasPermitidas = granjas
        _uiState.value = HistorialUiState()
        jobMediciones = viewModelScope.launch {
            repository.obtenerMediciones(puntoId, granjas).collect { measurements ->
                _uiState.update { it.copy(mediciones = measurements, cargando = false) }
            }
        }
    }

    fun registrarMedicion(puntoId: Int, nivel: Double, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                repository.registrarMedicion(puntoId, nivel, usuarioId, granjasPermitidas)
            }
            onCompleted(saved)
        }
    }
}
