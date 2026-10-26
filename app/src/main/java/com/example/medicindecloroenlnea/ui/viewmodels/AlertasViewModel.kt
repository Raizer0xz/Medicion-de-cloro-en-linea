package com.example.medicindecloroenlnea.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.CloroRepository
import com.example.medicindecloroenlnea.data.repository.FakeCloroRepository
import com.example.medicindecloroenlnea.domain.model.Alerta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertasUiState(
    val alertas: List<Alerta> = emptyList(),
    val cargando: Boolean = true
)

class AlertasViewModel(
    private val repository: CloroRepository = FakeCloroRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState: StateFlow<AlertasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerAlertas().collect { alertas ->
                _uiState.value = AlertasUiState(alertas = alertas, cargando = false)
            }
        }
    }
}
