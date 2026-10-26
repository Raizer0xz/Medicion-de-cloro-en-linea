package com.example.medicindecloroenlnea.ui.screens

import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.ui.viewmodels.GranjasUiState

val estadoEjemplo = GranjasUiState(
    granjas = listOf(Granja(1, "Granja Los Aromos"), Granja(2, "Granja El Roble")),
    puntos = listOf(
        PuntoMedicion(1, 1, "Galpón 1 - Bebederos", 0.9, EstadoCloro.NORMAL, "2026-10-03T08:30"),
        PuntoMedicion(2, 1, "Galpón 2 - Bebederos", 0.4, EstadoCloro.ADVERTENCIA, "2026-10-03T08:30"),
        PuntoMedicion(3, 1, "Estanque principal", 0.2, EstadoCloro.CRITICO, "2026-10-03T08:30"),
        PuntoMedicion(4, 1, "Sala de proceso", 1.7, EstadoCloro.ADVERTENCIA, "2026-10-03T08:30")
    ),
    granjaSeleccionada = 1,
    cargando = false
)