package com.example.medicindecloroenlnea

import com.example.medicindecloroenlnea.domain.model.DireccionDesviacion
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.RangoCloro
import com.example.medicindecloroenlnea.domain.model.calcularDireccion
import com.example.medicindecloroenlnea.domain.model.calcularEstado
import com.example.medicindecloroenlnea.domain.model.formatearNivel
import org.junit.Assert.assertEquals
import org.junit.Test

class CloroReglasTest {
    @Test
    fun clasificaLosLimitesDeLaGuia() {
        assertEquals(EstadoCloro.CRITICO, calcularEstado(1.99))
        assertEquals(EstadoCloro.ADVERTENCIA, calcularEstado(2.0))
        assertEquals(EstadoCloro.ADVERTENCIA, calcularEstado(2.99))
        assertEquals(EstadoCloro.NORMAL, calcularEstado(3.0))
        assertEquals(EstadoCloro.NORMAL, calcularEstado(5.0))
        assertEquals(EstadoCloro.ADVERTENCIA, calcularEstado(5.01))
        assertEquals(EstadoCloro.ADVERTENCIA, calcularEstado(6.0))
        assertEquals(EstadoCloro.CRITICO, calcularEstado(6.01))
    }

    @Test
    fun permiteUnRangoPorPunto() {
        val custom = RangoCloro(criticoBajo = 1.0, normalBajo = 2.0, normalAlto = 4.0, criticoAlto = 5.0)

        assertEquals(EstadoCloro.NORMAL, calcularEstado(3.0, custom))
        assertEquals(EstadoCloro.ADVERTENCIA, calcularEstado(4.5, custom))
        assertEquals(EstadoCloro.CRITICO, calcularEstado(5.1, custom))
    }

    @Test
    fun distingueLaDireccionDeLaDesviacionYFormateaDosDecimales() {
        assertEquals(DireccionDesviacion.INSUFICIENTE, calcularDireccion(2.5))
        assertEquals(DireccionDesviacion.EXCESIVO, calcularDireccion(5.5))
        assertEquals("3.10", formatearNivel(3.1))
    }
}
