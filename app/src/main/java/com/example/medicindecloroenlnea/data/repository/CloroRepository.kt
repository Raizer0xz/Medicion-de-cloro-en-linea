package com.example.medicindecloroenlnea.data.repository

import com.example.medicindecloroenlnea.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CloroRepository {
    fun obtenerGranjas(): Flow<List<Granja>>
    fun obtenerPuntos(granjaId: Int): Flow<List<PuntoMedicion>>
    fun obtenerMediciones(puntoId: Int): Flow<List<Medicion>>
    fun obtenerAlertas(): Flow<List<Alerta>>
}