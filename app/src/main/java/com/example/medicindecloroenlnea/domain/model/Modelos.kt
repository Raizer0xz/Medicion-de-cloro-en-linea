package com.example.medicindecloroenlnea.domain.model

enum class EstadoCloro { NORMAL, ADVERTENCIA, CRITICO }

data class Granja(
    val id: Int,
    val nombre: String
)

data class PuntoMedicion(
    val id: Int,
    val granjaId: Int,
    val nombre: String,
    val nivelActual: Double,          // cloro libre en mg/L
    val estado: EstadoCloro,
    val ultimaActualizacion: String   // "2026-10-03T08:30:00"
)

data class Medicion(
    val id: Int,
    val puntoId: Int,
    val nivelCloro: Double,
    val fechaHora: String,
    val estado: EstadoCloro
)

data class Alerta(
    val id: Int,
    val puntoId: Int,
    val nivelCloro: Double,
    val estado: EstadoCloro,
    val fechaHora: String,
    val atendida: Boolean
)

data class Accion(
    val id: Int,
    val puntoId: Int,
    val descripcion: String,
    val responsable: String,
    val fechaHora: String
)

// Regla única para decidir el estado (rangos de ejemplo, ajustar si el profe da otros)
fun calcularEstado(nivel: Double): EstadoCloro = when {
    nivel < 0.3 || nivel > 2.0 -> EstadoCloro.CRITICO
    nivel < 0.5 || nivel > 1.5 -> EstadoCloro.ADVERTENCIA
    else -> EstadoCloro.NORMAL
}