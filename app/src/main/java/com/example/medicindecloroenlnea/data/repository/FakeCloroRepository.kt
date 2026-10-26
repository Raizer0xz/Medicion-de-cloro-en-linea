package com.example.medicindecloroenlnea.data.repository

import com.example.medicindecloroenlnea.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCloroRepository : CloroRepository {

    private val granjas = listOf(
        Granja(1, "Granja Los Aromos"),
        Granja(2, "Granja El Roble"),
        Granja(3, "Granja Santa Rosa")
    )

    private fun punto(id: Int, granjaId: Int, nombre: String, nivel: Double) =
        PuntoMedicion(id, granjaId, nombre, nivel, calcularEstado(nivel), "2026-10-03T08:30:00")

    // Hay puntos en los tres estados para ver los colores del semáforo
    private val puntos = listOf(
        punto(1, 1, "Galpón 1 - Bebederos", 0.9),
        punto(2, 1, "Galpón 2 - Bebederos", 0.4),
        punto(3, 1, "Estanque principal", 0.2),
        punto(4, 2, "Galpón 1 - Bebederos", 1.1),
        punto(5, 2, "Sala de proceso", 1.7),
        punto(6, 3, "Galpón 1 - Bebederos", 0.8),
        punto(7, 3, "Estanque principal", 2.3)
    )

    // 5 mediciones por punto; la última coincide con el nivel actual
    private val mediciones = puntos.flatMap { p ->
        listOf(0.2, -0.1, 0.1, -0.05, 0.0).mapIndexed { i, delta ->
            val nivel = (p.nivelActual + delta).coerceAtLeast(0.0)
            Medicion(
                id = p.id * 10 + i,
                puntoId = p.id,
                nivelCloro = nivel,
                fechaHora = "2026-10-03T0${4 + i}:30:00",
                estado = calcularEstado(nivel)
            )
        }
    }

    private val alertas = puntos
        .filter { it.estado != EstadoCloro.NORMAL }
        .map {
            Alerta(it.id, it.id, it.nivelActual, it.estado, it.ultimaActualizacion, atendida = false)
        }

    override fun obtenerGranjas(): Flow<List<Granja>> = flowOf(granjas)
    override fun obtenerPuntos(granjaId: Int): Flow<List<PuntoMedicion>> =
        flowOf(puntos.filter { it.granjaId == granjaId })
    override fun obtenerMediciones(puntoId: Int): Flow<List<Medicion>> =
        flowOf(mediciones.filter { it.puntoId == puntoId })
    override fun obtenerAlertas(): Flow<List<Alerta>> = flowOf(alertas)
}