package com.example.medicindecloroenlnea.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicindecloroenlnea.data.repository.CloroRepository
import com.example.medicindecloroenlnea.data.repository.FakeCloroRepository
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResumenUiState(
    val total: Int = 0,
    val normales: Int = 0,
    val advertencias: Int = 0,
    val criticos: Int = 0
)

class ResumenViewModel(
    private val repository: CloroRepository = FakeCloroRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenUiState())
    val uiState: StateFlow<ResumenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerTodosLosPuntos().collect { puntos ->
                _uiState.value = ResumenUiState(
                    total = puntos.size,
                    normales = puntos.count { it.estado == EstadoCloro.NORMAL },
                    advertencias = puntos.count { it.estado == EstadoCloro.ADVERTENCIA },
                    criticos = puntos.count { it.estado == EstadoCloro.CRITICO }
                )
            }
        }
    }
}