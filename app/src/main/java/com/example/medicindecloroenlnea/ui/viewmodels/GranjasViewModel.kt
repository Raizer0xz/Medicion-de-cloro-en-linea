package com.example.medicindecloroenlnea.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.CloroRepository
import com.example.medicindecloroenlnea.data.repository.FakeCloroRepository
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la pantalla necesita mostrar, en un solo objeto
data class GranjasUiState(
    val granjas: List<Granja> = emptyList(),
    val puntos: List<PuntoMedicion> = emptyList(),
    val granjaSeleccionada: Int? = null,
    val cargando: Boolean = true
)

class GranjasViewModel(
    private val repository: CloroRepository = FakeCloroRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GranjasUiState())
    val uiState: StateFlow<GranjasUiState> = _uiState.asStateFlow()

    private var jobPuntos: Job? = null

    init {
        cargarGranjas()
    }

    private fun cargarGranjas() {
        viewModelScope.launch {
            repository.obtenerGranjas().collect { lista ->
                _uiState.update { it.copy(granjas = lista, cargando = false) }
                // Por defecto se muestra la primera granja
                if (_uiState.value.granjaSeleccionada == null && lista.isNotEmpty()) {
                    seleccionarGranja(lista.first().id)
                }
            }
        }
    }

    fun seleccionarGranja(granjaId: Int) {
        _uiState.update { it.copy(granjaSeleccionada = granjaId) }
        jobPuntos?.cancel()
        jobPuntos = viewModelScope.launch {
            repository.obtenerPuntos(granjaId).collect { puntos ->
                _uiState.update { it.copy(puntos = puntos) }
            }
        }
    }
}